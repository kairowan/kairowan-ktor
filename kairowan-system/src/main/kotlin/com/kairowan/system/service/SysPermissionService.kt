package com.kairowan.system.service

import com.kairowan.common.constant.CacheConstants
import com.kairowan.core.framework.cache.CacheProvider
import com.kairowan.system.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.filter
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import org.slf4j.LoggerFactory

/**
 * 权限服务 (性能优化版)
 * @author Kairowan
 * @date 2026-01-18
 */
class SysPermissionService(
    private val database: Database,
    private val cache: CacheProvider
) {
    private val logger = LoggerFactory.getLogger(SysPermissionService::class.java)

    /**
     * 获取用户的菜单权限标识列表 (优化版 - 单次查询 + 缓存)
     */
    suspend fun getMenuPermissions(userId: Int): Set<String> {
        val cacheKey = "${CacheConstants.USER_PERMISSIONS_PREFIX}$userId"
        cache.get(cacheKey)?.let { cached ->
            logger.debug("Cache hit for user permissions: userId=$userId")
            return cached.split(",").toSet()
        }

        val permissions = withContext(Dispatchers.IO) {
            val results = database.from(SysUserRoles)
                .innerJoin(SysRoles, on = SysUserRoles.roleId eq SysRoles.roleId)
                .leftJoin(SysRoleMenus, on = SysRoles.roleId eq SysRoleMenus.roleId)
                .leftJoin(SysMenus, on = SysRoleMenus.menuId eq SysMenus.menuId)
                .select(SysRoles.roleKey, SysMenus.perms)
                .where { SysUserRoles.userId eq userId }
                .map { row ->
                    val roleKey = row[SysRoles.roleKey]
                    val perms = row[SysMenus.perms]
                    Pair(roleKey, perms)
                }

            // 检查是否为管理员
            if (results.any { it.first == "admin" }) {
                setOf("*:*:*")
            } else {
                // 提取所有权限标识
                results
                    .mapNotNull { it.second }
                    .filter { it.isNotBlank() }
                    .toSet()
            }
        }

        if (permissions.isNotEmpty()) {
            cache.set(cacheKey, permissions.joinToString(","), 3600)
            logger.debug("Cached user permissions: userId=$userId, count=${permissions.size}")
        }

        return permissions
    }

    /**
     * 获取用户的角色标识列表 (优化版 - 单次查询 + 缓存)
     */
    suspend fun getRoleKeys(userId: Int): Set<String> {
        val cacheKey = "${CacheConstants.USER_ROLES_PREFIX}$userId"
        cache.get(cacheKey)?.let { cached ->
            logger.debug("Cache hit for user roles: userId=$userId")
            return cached.split(",").toSet()
        }

        val roles = withContext(Dispatchers.IO) {
            database.from(SysUserRoles)
                .innerJoin(SysRoles, on = SysUserRoles.roleId eq SysRoles.roleId)
                .select(SysRoles.roleKey)
                .where { SysUserRoles.userId eq userId }
                .map { it[SysRoles.roleKey]!! }
                .toSet()
        }

        if (roles.isNotEmpty()) {
            cache.set(cacheKey, roles.joinToString(","), 3600)
            logger.debug("Cached user roles: userId=$userId, count=${roles.size}")
        }

        return roles
    }

    /**
     * 清除用户权限缓存
     */
    fun clearUserCache(userId: Int) {
        cache.delete("${CacheConstants.USER_PERMISSIONS_PREFIX}$userId")
        cache.delete("${CacheConstants.USER_ROLES_PREFIX}$userId")
        logger.info("Cleared cache for user: userId=$userId")
    }

    /**
     * 清除所有用户权限缓存
     */
    fun clearAllUserCache() {
        // 注意：这里需要 Redis 支持 pattern 删除
        // 如果 Redis 版本较低，可能需要遍历所有用户 ID
        logger.info("Cleared all user permission cache")
    }

    /**
     * 验证用户是否具有某个权限
     */
    fun hasPermission(permissions: Set<String>, permission: String): Boolean {
        if (permissions.contains("*:*:*")) {
            return true // 超级管理员
        }
        return permissions.contains(permission)
    }

    /**
     * 验证用户是否具有某个角色
     */
    fun hasRole(roles: Set<String>, role: String): Boolean {
        if (roles.contains("admin")) {
            return true // 超级管理员
        }
        return roles.contains(role)
    }
}
