package com.kairowan.ktor.framework.web.service

import com.kairowan.ktor.framework.web.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.filter
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList

/**
 * 权限服务
 * @author Kairowan
 * @date 2026-01-18
 */
class SysPermissionService(private val database: Database) {

    /**
     * 获取用户的菜单权限标识列表
     */
    suspend fun getMenuPermissions(userId: Int): Set<String> = withContext(Dispatchers.IO) {
        // 查询用户角色
        val roleIds = database.from(SysUserRoles)
            .select(SysUserRoles.roleId)
            .where { SysUserRoles.userId eq userId }
            .map { it[SysUserRoles.roleId]!! }
            .toList()
        
        if (roleIds.isEmpty()) {
            return@withContext emptySet()
        }
        
        // 检查是否为管理员 (roleKey = admin)
        val isAdmin = database.sequenceOf(SysRoles)
            .filter { it.roleId inList roleIds }
            .toList()
            .any { it.roleKey == "admin" }
        
        if (isAdmin) {
            return@withContext setOf("*:*:*")  // 管理员拥有所有权限
        }
        
        // 查询角色对应的菜单权限
        val menuIds = database.from(SysRoleMenus)
            .select(SysRoleMenus.menuId)
            .where { SysRoleMenus.roleId inList roleIds }
            .map { it[SysRoleMenus.menuId]!! }
            .toList()
        
        if (menuIds.isEmpty()) {
            return@withContext emptySet()
        }
        
        // 获取权限标识
        database.sequenceOf(SysMenus)
            .filter { it.menuId inList menuIds }
            .toList()
            .map { it.perms }
            .filter { it.isNotBlank() }
            .toSet()
    }

    /**
     * 获取用户的角色标识列表
     */
    suspend fun getRoleKeys(userId: Int): Set<String> = withContext(Dispatchers.IO) {
        val roleIds = database.from(SysUserRoles)
            .select(SysUserRoles.roleId)
            .where { SysUserRoles.userId eq userId }
            .map { it[SysUserRoles.roleId]!! }
            .toList()
        
        if (roleIds.isEmpty()) {
            return@withContext emptySet()
        }
        
        database.sequenceOf(SysRoles)
            .filter { it.roleId inList roleIds }
            .toList()
            .map { it.roleKey }
            .toSet()
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
