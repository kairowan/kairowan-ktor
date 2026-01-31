package com.kairowan.core.plugin

/**
 * 性能监控配置
 */
class PerformanceMonitorConfig {
    /**
     * 慢接口阈值（毫秒）
     */
    var slowApiThreshold: Long = 100

    /**
     * 是否启用详细日志
     */
    var enableDetailedLogging: Boolean = false

    /**
     * 是否启用统计
     */
    var enableStats: Boolean = true
}
