package com.kairowan.ktor.framework.web.domain

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import org.ktorm.schema.datetime
import org.ktorm.entity.Entity
import java.time.LocalDateTime

/**
 * 系统菜单/权限实体
 * @author Kairowan
 * @date 2026-01-18
 */
interface SysMenu : Entity<SysMenu> {
    companion object : Entity.Factory<SysMenu>()
    
    var menuId: Int
    var menuName: String
    var parentId: Int        // 父菜单ID
    var orderNum: Int        // 显示顺序
    var path: String         // 路由地址
    var component: String?   // 组件路径
    var menuType: String     // M目录 C菜单 F按钮
    var perms: String        // 权限标识 (system:user:list)
    var icon: String
    var status: String       // 0正常 1停用
    var createTime: LocalDateTime?
}

object SysMenus : Table<SysMenu>("sys_menu") {
    val menuId = int("menu_id").primaryKey().bindTo { it.menuId }
    val menuName = varchar("menu_name").bindTo { it.menuName }
    val parentId = int("parent_id").bindTo { it.parentId }
    val orderNum = int("order_num").bindTo { it.orderNum }
    val path = varchar("path").bindTo { it.path }
    val component = varchar("component").bindTo { it.component }
    val menuType = varchar("menu_type").bindTo { it.menuType }
    val perms = varchar("perms").bindTo { it.perms }
    val icon = varchar("icon").bindTo { it.icon }
    val status = varchar("status").bindTo { it.status }
    val createTime = datetime("create_time").bindTo { it.createTime }
}
