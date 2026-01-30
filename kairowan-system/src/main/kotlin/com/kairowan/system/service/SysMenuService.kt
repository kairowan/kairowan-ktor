package com.kairowan.ktor.framework.web.service

import com.kairowan.ktor.framework.web.domain.SysMenu
import com.kairowan.ktor.framework.web.domain.SysMenus
import com.kairowan.ktor.framework.web.domain.SysRoleMenus
import com.kairowan.ktor.framework.web.domain.SysUserRoles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.filter
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList

/**
 * 菜单服务
 * @author Kairowan
 * @date 2026-01-18
 */
class SysMenuService(private val database: Database) : KService<SysMenu>(database, SysMenus) {

    /**
     * 根据用户ID查询菜单树 (用于前端动态路由)
     */
    suspend fun selectMenuTreeByUserId(userId: Int): List<RouterVo> = withContext(Dispatchers.IO) {
        // 查询用户角色
        val roleIds = database.from(SysUserRoles)
            .select(SysUserRoles.roleId)
            .where { SysUserRoles.userId eq userId }
            .map { it[SysUserRoles.roleId]!! }
            .toList()
        
        val menus: List<SysMenu> = if (roleIds.any { roleId -> 
            database.sequenceOf(com.kairowan.ktor.framework.web.domain.SysRoles)
                .filter { it.roleId eq roleId }
                .toList()
                .any { it.roleKey == "admin" }
        }) {
            // 管理员获取所有菜单
            database.sequenceOf(SysMenus)
                .filter { (it.menuType eq "M") or (it.menuType eq "C") }
                .filter { it.status eq "0" }
                .toList()
                .sortedBy { it.orderNum }
        } else {
            // 普通用户根据角色获取菜单
            val menuIds = database.from(SysRoleMenus)
                .select(SysRoleMenus.menuId)
                .where { SysRoleMenus.roleId inList roleIds }
                .map { it[SysRoleMenus.menuId]!! }
                .toList()
            
            if (menuIds.isEmpty()) {
                emptyList()
            } else {
                database.sequenceOf(SysMenus)
                    .filter { it.menuId inList menuIds }
                    .filter { (it.menuType eq "M") or (it.menuType eq "C") }
                    .filter { it.status eq "0" }
                    .toList()
                    .sortedBy { it.orderNum }
            }
        }
        
        buildMenuTree(menus, 0)
    }

    /**
     * 构建前端路由树
     */
    private fun buildMenuTree(menus: List<SysMenu>, parentId: Int): List<RouterVo> {
        return menus.filter { it.parentId == parentId }
            .map { menu ->
                RouterVo(
                    name = getRouteName(menu),
                    path = getRouterPath(menu),
                    component = menu.component ?: "",
                    meta = MetaVo(
                        title = menu.menuName,
                        icon = menu.icon,
                        noCache = false
                    ),
                    children = buildMenuTree(menus, menu.menuId)
                )
            }
    }

    private fun getRouteName(menu: SysMenu): String {
        return menu.path.replaceFirstChar { it.uppercase() }
    }

    private fun getRouterPath(menu: SysMenu): String {
        return if (menu.parentId == 0) "/${menu.path}" else menu.path
    }
}

/**
 * 路由配置 VO
 */
data class RouterVo(
    val name: String,
    val path: String,
    val component: String,
    val meta: MetaVo,
    val children: List<RouterVo> = emptyList()
)

data class MetaVo(
    val title: String,
    val icon: String,
    val noCache: Boolean = false
)
