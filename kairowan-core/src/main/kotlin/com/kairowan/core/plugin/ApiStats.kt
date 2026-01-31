package com.kairowan.core.plugin

import java.util.concurrent.atomic.AtomicLong

/**
 * 接口统计信息
 */
class ApiStats(val endpoint: String) {
    private val totalCount = AtomicLong(0)
    private val totalDuration = AtomicLong(0)
    private val errorCount = AtomicLong(0)
    private val slowCount = AtomicLong(0)
    private var minDuration = Long.MAX_VALUE
    private var maxDuration = Long.MIN_VALUE

    @Synchronized
    fun record(duration: Long, statusCode: Int) {
        totalCount.incrementAndGet()
        totalDuration.addAndGet(duration)

        if (statusCode >= 400) {
            errorCount.incrementAndGet()
        }

        if (duration > 100) {
            slowCount.incrementAndGet()
        }

        if (duration < minDuration) {
            minDuration = duration
        }

        if (duration > maxDuration) {
            maxDuration = duration
        }
    }

    fun getStats(): Map<String, Any> {
        val count = totalCount.get()
        val avgDuration = if (count > 0) totalDuration.get() / count else 0

        return mapOf(
            "endpoint" to endpoint,
            "totalCount" to count,
            "avgDuration" to avgDuration,
            "minDuration" to if (minDuration == Long.MAX_VALUE) 0 else minDuration,
            "maxDuration" to if (maxDuration == Long.MIN_VALUE) 0 else maxDuration,
            "errorCount" to errorCount.get(),
            "slowCount" to slowCount.get(),
            "errorRate" to if (count > 0) String.format("%.2f%%", errorCount.get() * 100.0 / count) else "0.00%",
            "slowRate" to if (count > 0) String.format("%.2f%%", slowCount.get() * 100.0 / count) else "0.00%"
        )
    }

    fun reset() {
        totalCount.set(0)
        totalDuration.set(0)
        errorCount.set(0)
        slowCount.set(0)
        minDuration = Long.MAX_VALUE
        maxDuration = Long.MIN_VALUE
    }

    override fun toString(): String {
        val stats = getStats()
        return """
            |ApiStats(
            |  endpoint=${stats["endpoint"]},
            |  totalCount=${stats["totalCount"]},
            |  avgDuration=${stats["avgDuration"]}ms,
            |  minDuration=${stats["minDuration"]}ms,
            |  maxDuration=${stats["maxDuration"]}ms,
            |  errorCount=${stats["errorCount"]} (${stats["errorRate"]}),
            |  slowCount=${stats["slowCount"]} (${stats["slowRate"]})
            |)
        """.trimMargin()
    }
}
