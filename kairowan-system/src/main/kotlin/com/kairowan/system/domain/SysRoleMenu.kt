package com.kairowan.system.domain

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.entity.Entity

/**
 * 角色-菜单关联实体
 * @author Kairowan
 * @date 2026-01-18
 */
interface SysRoleMenu : Entity<SysRoleMenu> {
    companion object : Entity.Factory<SysRoleMenu>()
    
    var roleId: Int
    var menuId: Int
}

object SysRoleMenus : Table<SysRoleMenu>("sys_role_menu") {
    val roleId = int("role_id").primaryKey().bindTo { it.roleId }
    val menuId = int("menu_id").primaryKey().bindTo { it.menuId }
}
