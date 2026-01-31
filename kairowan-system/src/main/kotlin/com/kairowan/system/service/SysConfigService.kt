package com.kairowan.system.service

import com.kairowan.core.framework.cache.CacheProvider
import com.kairowan.core.service.KService
import com.kairowan.system.domain.SysConfig
import com.kairowan.system.domain.SysConfigs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList

/**
 * 系统配置服务
 * @author Kairowan
 * @date 2026-01-18
 */
class SysConfigService(
    database: Database,
    private val cache: CacheProvider
) : KService<SysConfig>(database, SysConfigs) {

    companion object {
        private const val CACHE_PREFIX = "sys_config:"
    }

    /**
     * 根据配置键获取配置值 (优先从缓存获取)
     */
    suspend fun getConfigValue(configKey: String): String? {
        // 先从缓存获取
        val cached = cache.get("$CACHE_PREFIX$configKey")
        if (cached != null) {
            return cached
        }
        
        // 缓存未命中，从数据库获取
        val config = withContext(Dispatchers.IO) {
            database.sequenceOf(SysConfigs).find { it.configKey eq configKey }
        }
        
        config?.let {
            // 写入缓存
            cache.set("$CACHE_PREFIX$configKey", it.configValue)
        }
        
        return config?.configValue
    }

    /**
     * 根据配置键获取配置对象
     */
    suspend fun getByKey(configKey: String): SysConfig? = withContext(Dispatchers.IO) {
        database.sequenceOf(SysConfigs).find { it.configKey eq configKey }
    }

    /**
     * 刷新配置缓存
     */
    fun refreshCache(configKey: String, configValue: String) {
        cache.set("$CACHE_PREFIX$configKey", configValue)
    }

    /**
     * 刷新所有配置缓存
     */
    suspend fun refreshAllCache(): Int = withContext(Dispatchers.IO) {
        val configs = database.sequenceOf(SysConfigs).toList()
        configs.forEach { cfg ->
            cache.set("$CACHE_PREFIX${cfg.configKey}", cfg.configValue)
        }
        configs.size
    }

    /**
     * 清除配置缓存
     */
    fun clearCache(configKey: String) {
        cache.delete("$CACHE_PREFIX$configKey")
    }
}
