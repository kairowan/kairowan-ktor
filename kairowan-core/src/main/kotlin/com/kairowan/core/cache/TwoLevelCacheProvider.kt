package com.kairowan.core.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.kairowan.core.framework.cache.CacheProvider
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 * 两级缓存提供者
 * Two-Level Cache Provider (L1: Caffeine + L2: Redis)
 *
 * 缓存策略：
 * 1. 读取时先查 L1 (Caffeine)，未命中再查 L2 (Redis)
 * 2. 写入时同时写入 L1 和 L2
 * 3. 删除时同时删除 L1 和 L2
 *
 * @author Kairowan
 * @date 2026-01-28
 */
class TwoLevelCacheProvider(
    private val redisCache: CacheProvider
) : CacheProvider {

    private val logger = LoggerFactory.getLogger(TwoLevelCacheProvider::class.java)

    // L1 本地缓存 (Caffeine)
    private val localCache: Cache<String, String> = Caffeine.newBuilder()
        .maximumSize(5000)  // 最大缓存 5000 个 key
        .expireAfterWrite(5, TimeUnit.MINUTES)  // 5分钟过期
        .recordStats()  // 记录统计信息
        .build()

    /**
     * 获取缓存
     * 1. 先从 L1 获取
     * 2. L1 未命中，从 L2 获取
     * 3. L2 命中后写入 L1
     */
    override fun get(key: String): String? {
        // 1. 尝试从 L1 获取
        localCache.getIfPresent(key)?.let { value ->
            logger.debug("L1 cache hit: key=$key")
            return value
        }

        // 2. L1 未命中，从 L2 获取
        val value = redisCache.get(key)
        if (value != null) {
            logger.debug("L2 cache hit: key=$key")
            // 3. 写入 L1
            localCache.put(key, value)
        } else {
            logger.debug("Cache miss: key=$key")
        }

        return value
    }

    /**
     * 设置缓存
     * 同时写入 L1 和 L2
     */
    override fun set(key: String, value: String, expireSeconds: Int) {
        // 写入 L2 (Redis)
        redisCache.set(key, value, expireSeconds)

        // 写入 L1 (Caffeine)
        // L1 使用固定的 5 分钟过期时间
        localCache.put(key, value)

        logger.debug("Cache set: key=$key, expireSeconds=$expireSeconds")
    }

    /**
     * 删除缓存
     * 同时删除 L1 和 L2
     */
    override fun delete(key: String) {
        // 删除 L1
        localCache.invalidate(key)

        // 删除 L2
        redisCache.delete(key)

        logger.debug("Cache deleted: key=$key")
    }

    /**
     * 批量删除缓存（支持通配符）
     * 同时删除 L1 和 L2
     */
    override fun deleteByPattern(pattern: String) {
        // 删除 L2
        redisCache.deleteByPattern(pattern)

        // 删除 L1 中匹配的 key
        val keysToDelete = localCache.asMap().keys.filter { key ->
            matchPattern(key, pattern)
        }
        localCache.invalidateAll(keysToDelete)

        logger.debug("Cache deleted by pattern: pattern=$pattern, count=${keysToDelete.size}")
    }

    /**
     * 检查 key 是否存在
     */
    override fun exists(key: String): Boolean {
        // 先检查 L1
        if (localCache.getIfPresent(key) != null) {
            return true
        }

        // 再检查 L2
        return redisCache.exists(key)
    }

    /**
     * 设置过期时间
     * 只对 L2 (Redis) 有效
     */
    override fun expire(key: String, seconds: Int) {
        redisCache.expire(key, seconds)
    }

    /**
     * 获取 L1 缓存统计信息
     */
    fun getL1Stats(): CacheStats {
        val stats = localCache.stats()
        return CacheStats(
            hitCount = stats.hitCount(),
            missCount = stats.missCount(),
            hitRate = stats.hitRate(),
            evictionCount = stats.evictionCount(),
            size = localCache.estimatedSize()
        )
    }

    /**
     * 清空 L1 缓存
     */
    fun clearL1() {
        localCache.invalidateAll()
        logger.info("L1 cache cleared")
    }

    /**
     * 获取 L1 缓存大小
     */
    fun getL1Size(): Long {
        return localCache.estimatedSize()
    }

    /**
     * 简单的通配符匹配
     * 支持 * 通配符
     */
    private fun matchPattern(key: String, pattern: String): Boolean {
        val regex = pattern
            .replace("*", ".*")
            .replace("?", ".")
            .toRegex()
        return regex.matches(key)
    }
}
