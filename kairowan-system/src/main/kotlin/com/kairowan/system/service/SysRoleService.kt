package com.kairowan.system.service

import com.kairowan.common.constant.CacheConstants
import com.kairowan.core.framework.cache.CacheProvider
import com.kairowan.core.page.KPageRequest
import com.kairowan.core.page.KTableData
import com.kairowan.core.service.KService
import com.kairowan.core.extensions.toMap
import com.kairowan.system.domain.SysRole
import com.kairowan.system.domain.SysRoles
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.sortedBy
import org.ktorm.entity.toList
import org.ktorm.entity.find
import org.ktorm.entity.filter
import org.slf4j.LoggerFactory

/**
 * 角色服务 (性能优化版)
 * Role Service Implementation with Cache
 *
 * @author Kairowan
 * @date 2026-01-18
 */
class SysRoleService(
    database: Database,
    private val cache: CacheProvider
) : KService<SysRole>(database, SysRoles) {

    private val logger = LoggerFactory.getLogger(SysRoleService::class.java)
    private val mapper = jacksonObjectMapper()

    /**
     * 角色列表查询（带缓存）
     * 角色数据相对稳定，可以缓存全量数据
     */
    suspend fun listWithCache(page: KPageRequest?): KTableData {
        val cacheKey = "role:list:${page?.pageNum ?: "all"}:${page?.pageSize ?: "all"}"

        // 尝试从缓存获取
        cache.get(cacheKey)?.let { cached ->
            logger.debug("Cache hit for role list: page=${page?.pageNum ?: "all"}")
            return mapper.readValue(cached, KTableData::class.java)
        }

        val result = withContext(Dispatchers.IO) {
            if (page == null) {
                // 不分页，返回所有角色
                val list = database.sequenceOf(SysRoles)
                    .sortedBy { it.roleSort }
                    .toList()
                    .map { it.toMap() }
                KTableData.build(list)
            } else {
                val safePage = page.normalized()
                val offset = (safePage.pageNum - 1) * safePage.pageSize

                // 分页查询
                val allRoles = database.sequenceOf(SysRoles)
                    .sortedBy { it.roleSort }
                    .toList()

                val list = allRoles.drop(offset).take(safePage.pageSize)
                    .map { it.toMap() }

                // COUNT 查询
                val total = allRoles.size.toLong()

                KTableData.build(list, total)
            }
        }

        // 缓存1小时
        cache.set(cacheKey, mapper.writeValueAsString(result), CacheConstants.DEFAULT_EXPIRE_TIME)
        logger.debug("Cached role list: page=${page?.pageNum ?: "all"}")

        return result
    }

    /**
     * 根据角色ID查询角色（带缓存）
     */
    suspend fun getByRoleId(roleId: Int): SysRole? {
        val cacheKey = "role:info:$roleId"

        cache.get(cacheKey)?.let { cached ->
            logger.debug("Cache hit for role info: roleId=$roleId")
            return mapper.readValue(cached, SysRole::class.java)
        }

        val role = withContext(Dispatchers.IO) {
            database.sequenceOf(SysRoles).find { it.roleId eq roleId }
        }

        role?.let {
            cache.set(cacheKey, mapper.writeValueAsString(it), CacheConstants.DEFAULT_EXPIRE_TIME)
        }

        return role
    }

    /**
     * 获取所有角色（带缓存）
     * 用于下拉选择等场景
     */
    suspend fun getAllRoles(): List<SysRole> {
        val cacheKey = "role:all"

        cache.get(cacheKey)?.let { cached ->
            logger.debug("Cache hit for all roles")
            return mapper.readValue(cached)
        }

        val roles = withContext(Dispatchers.IO) {
            database.sequenceOf(SysRoles)
                .filter { it.status eq "0" }  // 只返回正常状态的角色
                .sortedBy { it.roleSort }
                .toList()
        }

        // 缓存1小时
        cache.set(cacheKey, mapper.writeValueAsString(roles), CacheConstants.DEFAULT_EXPIRE_TIME)

        return roles
    }

    /**
     * 清除角色缓存
     * 在角色新增、修改、删除时调用
     */
    fun clearRoleCache(roleId: Int? = null) {
        if (roleId != null) {
            // 清除指定角色缓存
            cache.delete("role:info:$roleId")
            logger.info("Cleared cache for role: roleId=$roleId")
        }

        // 清除所有角色列表缓存
        cache.deleteByPattern("role:list:*")
        cache.delete("role:all")
        logger.info("Cleared all role list cache")
    }

    /**
     * 清除所有角色相关缓存
     */
    fun clearAllRoleCache() {
        cache.deleteByPattern("role:*")
        logger.info("Cleared all role cache")
    }
}
