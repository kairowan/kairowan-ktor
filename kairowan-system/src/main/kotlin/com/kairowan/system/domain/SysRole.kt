package com.kairowan.system.domain

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import org.ktorm.schema.datetime
import org.ktorm.entity.Entity
import java.time.LocalDateTime

/**
 * 系统角色实体
 * @author Kairowan
 * @date 2026-01-18
 */
interface SysRole : Entity<SysRole> {
    companion object : Entity.Factory<SysRole>()
    
    var roleId: Int
    var roleName: String
    var roleKey: String      // 角色标识 (admin, common)
    var roleSort: Int
    var status: String       // 0正常 1停用
    var createTime: LocalDateTime?
}

object SysRoles : Table<SysRole>("sys_role") {
    val roleId = int("role_id").primaryKey().bindTo { it.roleId }
    val roleName = varchar("role_name").bindTo { it.roleName }
    val roleKey = varchar("role_key").bindTo { it.roleKey }
    val roleSort = int("role_sort").bindTo { it.roleSort }
    val status = varchar("status").bindTo { it.status }
    val createTime = datetime("create_time").bindTo { it.createTime }
}
