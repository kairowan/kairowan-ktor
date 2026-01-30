package com.kairowan.core.framework.cache

import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPool

/**
 * Redis 缓存提供者
 * Redis Cache Provider Implementation
 *
 * @author Kairowan
 * @date 2026-01-28
 */
class RedisCacheProvider(
    private val jedisPool: JedisPool
) : CacheProvider {

    private val logger = LoggerFactory.getLogger(RedisCacheProvider::class.java)

    override fun get(key: String): String? {
        return try {
            jedisPool.resource.use { jedis ->
                jedis.get(key)
            }
        } catch (e: Exception) {
            logger.error("Redis get error: key=$key", e)
            null
        }
    }

    override fun set(key: String, value: String, expireSeconds: Int) {
        try {
            jedisPool.resource.use { jedis ->
                jedis.setex(key, expireSeconds.toLong(), value)
            }
        } catch (e: Exception) {
            logger.error("Redis set error: key=$key", e)
        }
    }

    override fun delete(key: String) {
        try {
            jedisPool.resource.use { jedis ->
                jedis.del(key)
            }
        } catch (e: Exception) {
            logger.error("Redis delete error: key=$key", e)
        }
    }

    override fun deleteByPattern(pattern: String) {
        try {
            jedisPool.resource.use { jedis ->
                val keys = jedis.keys(pattern)
                if (keys.isNotEmpty()) {
                    jedis.del(*keys.toTypedArray())
                }
            }
        } catch (e: Exception) {
            logger.error("Redis deleteByPattern error: pattern=$pattern", e)
        }
    }

    override fun exists(key: String): Boolean {
        return try {
            jedisPool.resource.use { jedis ->
                jedis.exists(key)
            }
        } catch (e: Exception) {
            logger.error("Redis exists error: key=$key", e)
            false
        }
    }

    override fun expire(key: String, seconds: Int) {
        try {
            jedisPool.resource.use { jedis ->
                jedis.expire(key, seconds.toLong())
            }
        } catch (e: Exception) {
            logger.error("Redis expire error: key=$key", e)
        }
    }
}
