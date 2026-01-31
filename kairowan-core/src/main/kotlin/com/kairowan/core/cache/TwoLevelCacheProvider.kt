package com.kairowan.core.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.kairowan.core.framework.cache.CacheProvider
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 * 两级缓存提供者 (L1: Caffeine + L2: Redis)
 *
 * @author Kairowan
 * @date 2026-01-28
 */
class TwoLevelCacheProvider(
    private val redisCache: CacheProvider
) : CacheProvider {

    private val logger = LoggerFactory.getLogger(TwoLevelCacheProvider::class.java)

    private val localCache: Cache<String, String> = Caffeine.newBuilder()
        .maximumSize(5000)
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .recordStats()
        .build()

    override fun get(key: String): String? {
        localCache.getIfPresent(key)?.let { value ->
            logger.debug("L1 cache hit: key=$key")
            return value
        }

        val value = redisCache.get(key)
        if (value != null) {
            logger.debug("L2 cache hit: key=$key")
            localCache.put(key, value)
        } else {
            logger.debug("Cache miss: key=$key")
        }

        return value
    }

    override fun set(key: String, value: String, expireSeconds: Int) {
        redisCache.set(key, value, expireSeconds)
        localCache.put(key, value)
        logger.debug("Cache set: key=$key, expireSeconds=$expireSeconds")
    }

    override fun delete(key: String) {
        localCache.invalidate(key)
        redisCache.delete(key)
        logger.debug("Cache deleted: key=$key")
    }

    override fun deleteByPattern(pattern: String) {
        redisCache.deleteByPattern(pattern)

        val keysToDelete = localCache.asMap().keys.filter { key ->
            matchPattern(key, pattern)
        }
        localCache.invalidateAll(keysToDelete)

        logger.debug("Cache deleted by pattern: pattern=$pattern, count=${keysToDelete.size}")
    }

    override fun exists(key: String): Boolean {
        if (localCache.getIfPresent(key) != null) {
            return true
        }
        return redisCache.exists(key)
    }

    override fun expire(key: String, seconds: Int) {
        redisCache.expire(key, seconds)
    }

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

    fun clearL1() {
        localCache.invalidateAll()
        logger.info("L1 cache cleared")
    }

    fun getL1Size(): Long {
        return localCache.estimatedSize()
    }

    private fun matchPattern(key: String, pattern: String): Boolean {
        val regex = pattern
            .replace("*", ".*")
            .replace("?", ".")
            .toRegex()
        return regex.matches(key)
    }
}
