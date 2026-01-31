package com.kairowan.system.service

import com.kairowan.common.constant.CacheConstants
import com.kairowan.core.framework.cache.CacheProvider
import com.kairowan.system.domain.SysConfigs
import com.kairowan.system.domain.SysDictDatas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import org.slf4j.LoggerFactory

/**
 * 缓存预热服务
 * 系统启动时预加载热点数据到缓存
 */
class CacheWarmupService(
    private val database: Database,
    private val cache: CacheProvider
) {
    private val logger = LoggerFactory.getLogger(CacheWarmupService::class.java)

    /**
     * 预热所有缓存
     */
    suspend fun warmupAll() {
        logger.info("Starting cache warmup...")
        val startTime = System.currentTimeMillis()

        try {
            // 预热系统配置
            warmupSystemConfig()

            // 预热数据字典
            warmupDictData()

            val duration = System.currentTimeMillis() - startTime
            logger.info("Cache warmup completed in ${duration}ms")
        } catch (e: Exception) {
            logger.error("Cache warmup failed", e)
        }
    }

    /**
     * 预热系统配置缓存
     */
    private suspend fun warmupSystemConfig() = withContext(Dispatchers.IO) {
        val configs = database.sequenceOf(SysConfigs).toList()
        var count = 0

        configs.forEach { config ->
            cache.set(
                "${CacheConstants.CONFIG_PREFIX}${config.configKey}",
                config.configValue,
                CacheConstants.LONG_EXPIRE_TIME
            )
            count++
        }

        logger.info("Warmed up $count system configs")
    }

    /**
     * 预热数据字典缓存
     */
    private suspend fun warmupDictData() = withContext(Dispatchers.IO) {
        val dictTypes = database.sequenceOf(SysDictDatas)
            .filter { it.status eq "0" }
            .toList()
            .map { it.dictType }
            .distinct()

        var count = 0
        dictTypes.forEach { dictType ->
            val dictData = database.sequenceOf(SysDictDatas)
                .filter { (it.dictType eq dictType) and (it.status eq "0") }
                .toList()

            if (dictData.isNotEmpty()) {
                // 这里简化处理，实际应该序列化为 JSON
                cache.set(
                    "${CacheConstants.DICT_PREFIX}$dictType",
                    dictData.joinToString(",") { "${it.dictLabel}:${it.dictValue}" },
                    CacheConstants.LONG_EXPIRE_TIME
                )
                count++
            }
        }

        logger.info("Warmed up $count dict types")
    }
}
