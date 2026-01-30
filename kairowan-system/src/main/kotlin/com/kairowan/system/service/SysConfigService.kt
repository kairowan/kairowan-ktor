package com.kairowan.ktor.framework.web.service

import com.kairowan.ktor.core.cache.CacheProvider
import com.kairowan.ktor.core.database.DatabaseProvider
import com.kairowan.ktor.framework.web.domain.SysConfig
import com.kairowan.ktor.framework.web.domain.SysConfigs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.dsl.eq
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf

/**
 * 系统配置服务
 * @author Kairowan
 * @date 2026-01-18
 */
class SysConfigService(
    private val databaseProvider: DatabaseProvider,
    private val cache: CacheProvider
) : KService<SysConfig>(databaseProvider.database, SysConfigs) {

    private val database get() = databaseProvider.database

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
     * 清除配置缓存
     */
    fun clearCache(configKey: String) {
        cache.delete("$CACHE_PREFIX$configKey")
    }
}
