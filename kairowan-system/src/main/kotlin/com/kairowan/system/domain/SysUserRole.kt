package com.kairowan.system.domain

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.entity.Entity

/**
 * 用户-角色关联实体
 * @author Kairowan
 * @date 2026-01-18
 */
interface SysUserRole : Entity<SysUserRole> {
    companion object : Entity.Factory<SysUserRole>()
    
    var userId: Int
    var roleId: Int
}

object SysUserRoles : Table<SysUserRole>("sys_user_role") {
    val userId = int("user_id").primaryKey().bindTo { it.userId }
    val roleId = int("role_id").primaryKey().bindTo { it.roleId }
}
