package com.kairowan.core.plugin

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * 性能监控插件
 * Performance Monitor Plugin
 *
 * 功能：
 * 1. 记录每个接口的响应时间
 * 2. 统计慢接口（> 100ms）
 * 3. 统计接口调用次数
 * 4. 提供性能报告
 *
 * @author Kairowan
 * @date 2026-01-28
 */

val PerformanceMonitor = createApplicationPlugin(
    name = "PerformanceMonitor",
    createConfiguration = ::PerformanceMonitorConfig
) {
    val logger = LoggerFactory.getLogger("PerformanceMonitor")
    val config = pluginConfig

    // 接口统计信息
    val apiStats = ConcurrentHashMap<String, ApiStats>()

    onCall { call ->
        val startTime = System.currentTimeMillis()
        val uri = call.request.uri
        val method = call.request.httpMethod.value

        // 在响应发送后记录性能数据
        call.response.pipeline.intercept(ApplicationSendPipeline.After) {
            val duration = System.currentTimeMillis() - startTime
            val statusCode = call.response.status()?.value ?: 0

            // 记录慢接口
            if (duration > config.slowApiThreshold) {
                logger.warn(
                    "Slow API detected: $method $uri took ${duration}ms (status: $statusCode)"
                )
            }

            // 详细日志
            if (config.enableDetailedLogging) {
                logger.debug(
                    "API: $method $uri | Duration: ${duration}ms | Status: $statusCode"
                )
            }

            // 统计信息
            if (config.enableStats) {
                val key = "$method $uri"
                apiStats.computeIfAbsent(key) { ApiStats(key) }.record(duration, statusCode)
            }
        }
    }

    // 提供获取统计信息的方法
    application.attributes.put(ApiStatsKey, apiStats)
}

/**
 * 接口统计信息
 */
val ApiStatsKey = AttributeKey<ConcurrentHashMap<String, ApiStats>>("ApiStats")

/**
 * 获取性能统计信息
 */
fun Application.getPerformanceStats(): Map<String, Map<String, Any>> {
    val apiStats = attributes.getOrNull(ApiStatsKey) ?: return emptyMap()
    return apiStats.mapValues { it.value.getStats() }
}

/**
 * 获取慢接口列表
 */
fun Application.getSlowApis(limit: Int = 10): List<Map<String, Any>> {
    val apiStats = attributes.getOrNull(ApiStatsKey) ?: return emptyList()
    return apiStats.values
        .map { it.getStats() }
        .sortedByDescending { it["avgDuration"] as Long }
        .take(limit)
}

/**
 * 获取错误接口列表
 */
fun Application.getErrorApis(limit: Int = 10): List<Map<String, Any>> {
    val apiStats = attributes.getOrNull(ApiStatsKey) ?: return emptyList()
    return apiStats.values
        .map { it.getStats() }
        .filter { (it["errorCount"] as Long) > 0 }
        .sortedByDescending { it["errorCount"] as Long }
        .take(limit)
}

/**
 * 重置统计信息
 */
fun Application.resetPerformanceStats() {
    val apiStats = attributes.getOrNull(ApiStatsKey)
    apiStats?.clear()
}
