package com.kairowan.monitor.service

import com.kairowan.core.framework.cache.CacheProvider
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.management.ManagementFactory
import java.net.InetAddress
import java.text.DecimalFormat

/**
 * 服务器监控服务 (性能优化版)
 * Server Monitor Service with Cache
 *
 * @author Kairowan
 * @date 2026-01-18
 */
class ServerMonitorService(
    private val cache: CacheProvider
) {
    private val logger = LoggerFactory.getLogger(ServerMonitorService::class.java)
    private val mapper = jacksonObjectMapper()
    private val df = DecimalFormat("#.##")

    /**
     * 获取服务器完整信息（带缓存）
     * 缓存1分钟，避免频繁收集系统信息
     */
    fun getServerInfo(): ServerInfo {
        val cacheKey = "server:info"

        // 从缓存获取（1分钟）
        cache.get(cacheKey)?.let { cached ->
            logger.debug("Cache hit for server info")
            return mapper.readValue(cached, ServerInfo::class.java)
        }

        // 收集服务器信息
        val serverInfo = ServerInfo(
            cpu = getCpuInfo(),
            memory = getMemoryInfo(),
            jvm = getJvmInfo(),
            system = getSystemInfo(),
            disk = getDiskInfo()
        )

        // 缓存1分钟
        cache.set(cacheKey, mapper.writeValueAsString(serverInfo), 60)
        logger.debug("Cached server info")

        return serverInfo
    }

    /**
     * CPU 信息
     */
    private fun getCpuInfo(): CpuInfo {
        val osBean = ManagementFactory.getOperatingSystemMXBean()
        val processors = Runtime.getRuntime().availableProcessors()
        
        // 系统平均负载 (仅 Unix)
        val loadAverage = osBean.systemLoadAverage
        val cpuUsage = if (loadAverage >= 0) {
            df.format((loadAverage / processors) * 100)
        } else {
            "N/A"
        }
        
        return CpuInfo(
            cpuNum = processors,
            usagePercent = cpuUsage,
            loadAverage = if (loadAverage >= 0) df.format(loadAverage) else "N/A"
        )
    }

    /**
     * 内存信息
     */
    private fun getMemoryInfo(): MemoryInfo {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val usedMemory = totalMemory - freeMemory
        
        return MemoryInfo(
            total = formatBytes(maxMemory),
            used = formatBytes(usedMemory),
            free = formatBytes(maxMemory - usedMemory),
            usagePercent = df.format(usedMemory.toDouble() / maxMemory * 100)
        )
    }

    /**
     * JVM 信息
     */
    private fun getJvmInfo(): JvmInfo {
        val runtime = ManagementFactory.getRuntimeMXBean()
        val startTime = runtime.startTime
        val uptime = runtime.uptime
        
        return JvmInfo(
            name = runtime.vmName,
            version = System.getProperty("java.version"),
            vendor = System.getProperty("java.vendor"),
            home = System.getProperty("java.home"),
            startTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(java.util.Date(startTime)),
            runTime = formatUptime(uptime),
            inputArgs = runtime.inputArguments.take(5).joinToString(" ")
        )
    }

    /**
     * 系统信息
     */
    private fun getSystemInfo(): SystemInfo {
        val osName = System.getProperty("os.name")
        val osArch = System.getProperty("os.arch")
        val osVersion = System.getProperty("os.version")
        val userDir = System.getProperty("user.dir")
        
        val hostName = try {
            InetAddress.getLocalHost().hostName
        } catch (e: Exception) {
            "Unknown"
        }
        
        val hostAddress = try {
            InetAddress.getLocalHost().hostAddress
        } catch (e: Exception) {
            "Unknown"
        }
        
        return SystemInfo(
            osName = osName,
            osArch = osArch,
            osVersion = osVersion,
            hostName = hostName,
            hostAddress = hostAddress,
            userDir = userDir
        )
    }

    /**
     * 磁盘信息
     */
    private fun getDiskInfo(): List<DiskInfo> {
        return File.listRoots().map { root ->
            DiskInfo(
                path = root.path,
                total = formatBytes(root.totalSpace),
                free = formatBytes(root.freeSpace),
                used = formatBytes(root.totalSpace - root.freeSpace),
                usagePercent = if (root.totalSpace > 0) {
                    df.format((root.totalSpace - root.freeSpace).toDouble() / root.totalSpace * 100)
                } else "0"
            )
        }
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1073741824 -> df.format(bytes / 1073741824.0) + " GB"
            bytes >= 1048576 -> df.format(bytes / 1048576.0) + " MB"
            bytes >= 1024 -> df.format(bytes / 1024.0) + " KB"
            else -> "$bytes B"
        }
    }

    private fun formatUptime(millis: Long): String {
        val seconds = millis / 1000
        val days = seconds / 86400
        val hours = (seconds % 86400) / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        
        return buildString {
            if (days > 0) append("${days}天 ")
            if (hours > 0 || days > 0) append("${hours}小时 ")
            if (minutes > 0 || hours > 0 || days > 0) append("${minutes}分钟 ")
            append("${secs}秒")
        }
    }
}

/**
 * 服务器信息
 */
data class ServerInfo(
    val cpu: CpuInfo,
    val memory: MemoryInfo,
    val jvm: JvmInfo,
    val system: SystemInfo,
    val disk: List<DiskInfo>
)

data class CpuInfo(
    val cpuNum: Int,
    val usagePercent: String,
    val loadAverage: String
)

data class MemoryInfo(
    val total: String,
    val used: String,
    val free: String,
    val usagePercent: String
)

data class JvmInfo(
    val name: String,
    val version: String,
    val vendor: String,
    val home: String,
    val startTime: String,
    val runTime: String,
    val inputArgs: String
)

data class SystemInfo(
    val osName: String,
    val osArch: String,
    val osVersion: String,
    val hostName: String,
    val hostAddress: String,
    val userDir: String
)

data class DiskInfo(
    val path: String,
    val total: String,
    val free: String,
    val used: String,
    val usagePercent: String
)
