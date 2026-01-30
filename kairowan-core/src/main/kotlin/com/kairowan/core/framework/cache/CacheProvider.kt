package com.kairowan.core.framework.cache

/**
 * 缓存提供者接口
 * Cache Provider Interface
 *
 * @author Kairowan
 * @date 2026-01-28
 */
interface CacheProvider {
    /**
     * 获取缓存
     */
    fun get(key: String): String?

    /**
     * 设置缓存
     */
    fun set(key: String, value: String, expireSeconds: Int = 3600)

    /**
     * 删除缓存
     */
    fun delete(key: String)

    /**
     * 批量删除缓存（支持通配符）
     */
    fun deleteByPattern(pattern: String)

    /**
     * 检查 key 是否存在
     */
    fun exists(key: String): Boolean

    /**
     * 设置过期时间
     */
    fun expire(key: String, seconds: Int)
}
