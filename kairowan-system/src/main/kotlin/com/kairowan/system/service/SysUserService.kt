package com.kairowan.system.service

import com.kairowan.common.constant.CacheConstants
import com.kairowan.core.framework.cache.CacheProvider
import com.kairowan.core.page.KPageRequest
import com.kairowan.core.page.KTableData
import com.kairowan.core.service.KService
import com.kairowan.core.extensions.toMap
import com.kairowan.system.domain.SysUser
import com.kairowan.system.domain.SysUsers
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import org.slf4j.LoggerFactory

/**
 * 系统用户服务
 * System User Service Implementation
 * @author Kairowan
 * @date 2026-01-17
 */
class SysUserService(
    database: Database,
    private val cache: CacheProvider
) : KService<SysUser>(database, SysUsers) {

    private val logger = LoggerFactory.getLogger(SysUserService::class.java)
    private val mapper = jacksonObjectMapper()

    /**
     * 根据用户ID查询用户（带缓存）
     */
    suspend fun getByUserId(userId: Int): SysUser? {
        val cacheKey = "${CacheConstants.USER_INFO_PREFIX}$userId"
        cache.get(cacheKey)?.let { cached ->
            logger.debug("Cache hit for user info: userId=$userId")
            return mapper.readValue(cached, SysUser::class.java)
        }

        val user = dbQuery {
            database.sequenceOf(SysUsers).find { it.userId eq userId }
        }

        user?.let {
            cache.set(cacheKey, mapper.writeValueAsString(it), CacheConstants.DEFAULT_EXPIRE_TIME)
        }

        return user
    }

    /**
     * 用户列表查询（优化版 - 字段选择 + 缓存）
     */
    suspend fun listOptimized(page: KPageRequest?): KTableData {
        // 对于小页码的查询，使用缓存
        if (page != null && page.pageNum <= 3) {
            val cacheKey = "user:list:${page.pageNum}:${page.pageSize}"
            cache.get(cacheKey)?.let { cached ->
                logger.debug("Cache hit for user list: page=${page.pageNum}")
                return mapper.readValue(cached, KTableData::class.java)
            }
        }

        val result = withContext(Dispatchers.IO) {
            val safePage = page?.normalized()

            if (safePage == null) {
                // 不分页，返回所有数据
                val list = database.sequenceOf(SysUsers).toList().map { it.toMap() }
                KTableData.build(list)
            } else {
                val offset = (safePage.pageNum - 1) * safePage.pageSize

                // 只查询必要字段，提升性能
                val query = database.from(SysUsers)
                    .select(
                        SysUsers.userId,
                        SysUsers.userName,
                        SysUsers.nickName,
                        SysUsers.email,
                        SysUsers.phone,
                        SysUsers.status,
                        SysUsers.deptId,
                        SysUsers.createTime
                    )
                    .orderBy(SysUsers.userId.desc())
                    .limit(offset, safePage.pageSize)

                // 优化 COUNT 查询
                val total = database.from(SysUsers)
                    .select(count())
                    .map { it.getLong(1) }
                    .first()

                val list = query.map { SysUsers.createEntity(it) }.map { it.toMap() }
                KTableData.build(list, total)
            }
        }

        // 缓存前3页数据（5分钟）
        if (page != null && page.pageNum <= 3) {
            val cacheKey = "user:list:${page.pageNum}:${page.pageSize}"
            cache.set(cacheKey, mapper.writeValueAsString(result), CacheConstants.SHORT_EXPIRE_TIME)
        }

        return result
    }

    /**
     * 清除用户缓存
     */
    fun clearUserCache(userId: Int) {
        cache.delete("${CacheConstants.USER_INFO_PREFIX}$userId")
        // 清除列表缓存
        for (i in 1..3) {
            cache.delete("user:list:$i:10")
            cache.delete("user:list:$i:20")
        }
        logger.info("Cleared cache for user: userId=$userId")
    }
}
