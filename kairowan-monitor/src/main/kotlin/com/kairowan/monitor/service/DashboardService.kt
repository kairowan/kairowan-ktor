package com.kairowan.monitor.service

import com.kairowan.monitor.domain.SysLoginLogs
import com.kairowan.monitor.domain.SysOperLogs
import com.kairowan.system.domain.SysUsers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.lang.management.ManagementFactory
import java.time.LocalDateTime

/**
 * 仪表盘服务
 * @author Kairowan
 * @date 2026-01-29
 */
class DashboardService(private val database: Database) {

    /**
     * 获取统计数据
     */
    suspend fun getStats(): Map<String, Any> = withContext(Dispatchers.IO) {
        // 获取用户总数
        val userCount = database.from(SysUsers)
            .select(count())
            .map { it.getInt(1) }
            .firstOrNull() ?: 0

        // 获取今日访问（今日登录次数）
        val visitCount = database.from(SysLoginLogs)
            .select(count())
            .where {
                SysLoginLogs.loginTime greaterEq LocalDateTime.now().toLocalDate().atStartOfDay()
            }
            .map { it.getInt(1) }
            .firstOrNull() ?: 0

        // 消息数量（暂时返回0，后续实现通知功能后更新）
        val messageCount = 0

        // 订单数量（暂时返回今日操作次数作为示例）
        val orderCount = database.from(SysOperLogs)
            .select(count())
            .where {
                SysOperLogs.operTime greaterEq LocalDateTime.now().toLocalDate().atStartOfDay()
            }
            .map { it.getInt(1) }
            .firstOrNull() ?: 0

        mapOf(
            "userCount" to userCount,
            "visitCount" to visitCount,
            "messageCount" to messageCount,
            "orderCount" to orderCount
        )
    }

    /**
     * 获取系统信息
     */
    suspend fun getSystemInfo(): Map<String, Any> = withContext(Dispatchers.IO) {
        mapOf(
            "version" to "v1.0.0",
            "backendFramework" to "Ktor 3.0",
            "frontendFramework" to "Vue 3.5",
            "uiFramework" to "Element Plus 2.13"
        )
    }
}
