package com.kairowan.ktor.common.utils

import com.kairowan.ktor.framework.config.RedisConfig
import redis.clients.jedis.Jedis

/**
 * Redis 工具类
 * @author Kairowan
 * @date 2026-01-17
 */
object RedisUtils {
    
    private fun <T> use(block: (Jedis) -> T): T {
        return RedisConfig.pool.resource.use { block(it) }
    }
    
    fun set(key: String, value: String) = use { it.set(key, value) }
    
    fun set(key: String, value: String, expireSeconds: Long) = use { 
        it.setex(key, expireSeconds, value) 
    }
    
    /**
     * 设置带过期时间的值 (秒)
     */
    fun setex(key: String, seconds: Int, value: String) = use { 
        it.setex(key, seconds.toLong(), value) 
    }
    
    fun get(key: String): String? = use { it.get(key) }
    
    fun del(key: String) = use { it.del(key) }
    
    fun exists(key: String): Boolean = use { it.exists(key) }
    
    /**
     * 根据模式获取所有匹配的键
     * @param pattern 匹配模式，如 "prefix:*"
     */
    fun keys(pattern: String): Set<String> = use { it.keys(pattern) }
    
    /**
     * 设置过期时间 (秒)
     */
    fun expire(key: String, seconds: Long) = use { it.expire(key, seconds) }
    
    /**
     * 获取剩余过期时间 (秒)
     */
    fun ttl(key: String): Long = use { it.ttl(key) }
    
    /**
     * 自增
     */
    fun incr(key: String): Long = use { it.incr(key) }
    
    /**
     * Hash 设置
     */
    fun hset(key: String, field: String, value: String) = use { it.hset(key, field, value) }
    
    /**
     * Hash 获取
     */
    fun hget(key: String, field: String): String? = use { it.hget(key, field) }
    
    /**
     * Hash 删除
     */
    fun hdel(key: String, vararg fields: String) = use { it.hdel(key, *fields) }
    
    /**
     * Hash 获取所有
     */
    fun hgetAll(key: String): Map<String, String> = use { it.hgetAll(key) }
}

