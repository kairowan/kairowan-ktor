package com.kairowan.core.cache

/**
 * 缓存统计信息
 */
data class CacheStats(
    val hitCount: Long,
    val missCount: Long,
    val hitRate: Double,
    val evictionCount: Long,
    val size: Long
) {
    val totalCount: Long get() = hitCount + missCount
    val missRate: Double get() = 1.0 - hitRate

    override fun toString(): String {
        return """
            |CacheStats(
            |  size=$size,
            |  hitCount=$hitCount,
            |  missCount=$missCount,
            |  hitRate=${String.format("%.2f%%", hitRate * 100)},
            |  missRate=${String.format("%.2f%%", missRate * 100)},
            |  evictionCount=$evictionCount
            |)
        """.trimMargin()
    }
}
