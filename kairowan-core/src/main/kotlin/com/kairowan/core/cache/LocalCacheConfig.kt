package com.kairowan.core.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.TimeUnit

/**
 * 本地缓存配置 (Caffeine)
 * Local Cache Configuration
 *
 * 用于缓存热点数据，减少 Redis 网络开销
 * 适合缓存：用户权限、菜单树等高频访问数据
 *
 * @author Kairowan
 * @date 2026-01-28
 */
object LocalCacheConfig {

    /**
     * 用户信息缓存
     * - 最大容量: 1000 个用户
     * - 过期时间: 5 分钟
     * - 适用场景: 用户详情查询
     */
    val userCache: Cache<String, Any> = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .recordStats()
        .build()

    /**
     * 用户权限缓存
     * - 最大容量: 1000 个用户
     * - 过期时间: 30 分钟
     * - 适用场景: 权限检查
     */
    val permissionCache: Cache<String, Set<String>> = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .recordStats()
        .build()

    /**
     * 用户角色缓存
     * - 最大容量: 1000 个用户
     * - 过期时间: 30 分钟
     * - 适用场景: 角色检查
     */
    val roleCache: Cache<String, Set<String>> = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .recordStats()
        .build()

    /**
     * 菜单树缓存
     * - 最大容量: 500 个用户
     * - 过期时间: 30 分钟
     * - 适用场景: 菜单加载
     */
    val menuTreeCache: Cache<String, List<Any>> = Caffeine.newBuilder()
        .maximumSize(500)
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .recordStats()
        .build()

    /**
     * 系统配置缓存
     * - 最大容量: 200 个配置项
     * - 过期时间: 1 小时
     * - 适用场景: 系统配置读取
     */
    val configCache: Cache<String, String> = Caffeine.newBuilder()
        .maximumSize(200)
        .expireAfterWrite(1, TimeUnit.HOURS)
        .recordStats()
        .build()

    /**
     * 数据字典缓存
     * - 最大容量: 500 个字典项
     * - 过期时间: 1 小时
     * - 适用场景: 字典数据读取
     */
    val dictCache: Cache<String, List<Any>> = Caffeine.newBuilder()
        .maximumSize(500)
        .expireAfterWrite(1, TimeUnit.HOURS)
        .recordStats()
        .build()

    /**
     * 获取所有缓存的统计信息
     */
    fun getAllStats(): Map<String, CacheStats> {
        return mapOf(
            "user" to getCacheStats(userCache),
            "permission" to getCacheStats(permissionCache),
            "role" to getCacheStats(roleCache),
            "menuTree" to getCacheStats(menuTreeCache),
            "config" to getCacheStats(configCache),
            "dict" to getCacheStats(dictCache)
        )
    }

    /**
     * 获取单个缓存的统计信息
     */
    private fun <K, V> getCacheStats(cache: Cache<K, V>): CacheStats {
        val stats = cache.stats()
        return CacheStats(
            hitCount = stats.hitCount(),
            missCount = stats.missCount(),
            hitRate = stats.hitRate(),
            evictionCount = stats.evictionCount(),
            size = cache.estimatedSize()
        )
    }

    /**
     * 清除所有本地缓存
     */
    fun clearAll() {
        userCache.invalidateAll()
        permissionCache.invalidateAll()
        roleCache.invalidateAll()
        menuTreeCache.invalidateAll()
        configCache.invalidateAll()
        dictCache.invalidateAll()
    }

    /**
     * 清除指定用户的缓存
     */
    fun clearUserCache(userId: Int) {
        val userIdStr = userId.toString()
        userCache.invalidate("user:info:$userIdStr")
        permissionCache.invalidate("user:permissions:$userIdStr")
        roleCache.invalidate("user:roles:$userIdStr")
        menuTreeCache.invalidate("user:menu:tree:$userIdStr")
    }
}

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
