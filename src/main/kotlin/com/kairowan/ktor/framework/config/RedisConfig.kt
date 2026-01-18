package com.kairowan.ktor.framework.config

import io.ktor.server.config.*
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

/**
 * Redis 配置与连接池
 * @author Kairowan
 * @date 2026-01-17
 */
object RedisConfig {
    lateinit var pool: JedisPool
    
    fun init(config: ApplicationConfig) {
        val host = config.property("redis.host").getString()
        val port = config.property("redis.port").getString().toInt()
        val password = config.propertyOrNull("redis.password")?.getString()?.takeIf { it.isNotEmpty() }
        val timeout = config.property("redis.timeout").getString().toInt()
        
        val poolConfig = JedisPoolConfig().apply {
            maxTotal = 20
            maxIdle = 10
            minIdle = 5
        }
        
        pool = if (password != null) {
            JedisPool(poolConfig, host, port, timeout, password)
        } else {
            JedisPool(poolConfig, host, port, timeout)
        }
    }
}
