package com.kairowan.system.service

import com.kairowan.common.constant.CacheConstants
import com.kairowan.core.framework.cache.CacheProvider
import com.kairowan.core.service.KService
import com.kairowan.system.domain.SysMenu
import com.kairowan.system.domain.SysMenus
import com.kairowan.system.domain.SysRoleMenus
import com.kairowan.system.domain.SysRoles
import com.kairowan.system.domain.SysUserRoles
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.filter
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import org.slf4j.LoggerFactory

/**
 * 菜单服务 (性能优化版)
 * @author Kairowan
 * @date 2026-01-18
 */
class SysMenuService(
    database: Database,
    private val cache: CacheProvider
) : KService<SysMenu>(database, SysMenus) {

    private val logger = LoggerFactory.getLogger(SysMenuService::class.java)
    private val mapper = jacksonObjectMapper()

    /**
     * 根据用户ID查询菜单树 (优化版 - 单次查询 + 缓存)
     */
    suspend fun selectMenuTreeByUserId(userId: Int): List<RouterVo> {
        // 1. 先从缓存获取
        val cacheKey = "${CacheConstants.USER_MENU_TREE_PREFIX}$userId"
        cache.get(cacheKey)?.let { cached ->
            logger.debug("Cache hit for user menu tree: userId=$userId")
            return try {
                mapper.readValue(cached)
            } catch (e: Exception) {
                logger.error("Failed to deserialize cached menu tree", e)
                cache.delete(cacheKey)
                null
            } ?: emptyList()
        }

        // 2. 缓存未命中，从数据库查询
        val menuTree = try {
            withContext(Dispatchers.IO) {
                // 简化查询：先检查表是否存在数据
                val hasData = try {
                    database.from(SysMenus).select(count()).map { it.getInt(1) }.firstOrNull() ?: 0
                } catch (e: Exception) {
                    logger.error("Failed to check menu table", e)
                    0
                }

                if (hasData == 0) {
                    logger.warn("No menu data found in database")
                    return@withContext emptyList<RouterVo>()
                }

                // 查询用户的所有菜单（简化版）
                val menus = database.from(SysUserRoles)
                    .innerJoin(SysRoles, on = SysUserRoles.roleId eq SysRoles.roleId)
                    .leftJoin(SysRoleMenus, on = SysRoles.roleId eq SysRoleMenus.roleId)
                    .leftJoin(SysMenus, on = SysRoleMenus.menuId eq SysMenus.menuId)
                    .select(
                        SysRoles.roleKey,
                        SysMenus.menuId,
                        SysMenus.menuName,
                        SysMenus.parentId,
                        SysMenus.orderNum,
                        SysMenus.path,
                        SysMenus.component,
                        SysMenus.menuType,
                        SysMenus.status,
                        SysMenus.icon
                    )
                    .where {
                        (SysUserRoles.userId eq userId) and
                        ((SysMenus.menuType eq "M") or (SysMenus.menuType eq "C")) and
                        (SysMenus.status eq "0")
                    }
                    .map { row ->
                        val roleKey = row[SysRoles.roleKey]
                        val menuId = row[SysMenus.menuId]

                        if (roleKey == "admin" || menuId != null) {
                            SysMenu {
                                this.menuId = menuId ?: 0
                                this.menuName = row[SysMenus.menuName] ?: ""
                                this.parentId = row[SysMenus.parentId] ?: 0
                                this.orderNum = row[SysMenus.orderNum] ?: 0
                                this.path = row[SysMenus.path] ?: ""
                                this.component = row[SysMenus.component]
                                this.menuType = row[SysMenus.menuType] ?: ""
                                this.status = row[SysMenus.status] ?: ""
                                this.icon = row[SysMenus.icon] ?: ""
                            }
                        } else null
                    }
                    .filterNotNull()
                    .distinctBy { it.menuId }
                    .sortedBy { it.orderNum }

                buildMenuTree(menus, 0)
            }
        } catch (e: Exception) {
            logger.error("Failed to query menu tree for userId=$userId", e)
            // 返回空列表，避免阻塞用户登录
            emptyList()
        }

        // 3. 写入缓存 (30分钟过期)
        if (menuTree.isNotEmpty()) {
            try {
                cache.set(cacheKey, mapper.writeValueAsString(menuTree), 1800)
                logger.debug("Cached user menu tree: userId=$userId, count=${menuTree.size}")
            } catch (e: Exception) {
                logger.error("Failed to cache menu tree", e)
            }
        }

        return menuTree
    }

    /**
     * 获取所有菜单列表（带缓存）
     */
    suspend fun listWithCache(): List<SysMenu> {
        val cacheKey = "menu:list:all"

        cache.get(cacheKey)?.let { cached ->
            logger.debug("Cache hit for menu list")
            return mapper.readValue(cached)
        }

        val menus = withContext(Dispatchers.IO) {
            database.sequenceOf(SysMenus)
                .filter { it.status eq "0" }
                .toList()
                .sortedBy { it.orderNum }
        }

        // 缓存1小时
        cache.set(cacheKey, mapper.writeValueAsString(menus), CacheConstants.DEFAULT_EXPIRE_TIME)

        return menus
    }

    /**
     * 获取菜单树（带缓存）
     */
    suspend fun getMenuTree(): List<SysMenu> {
        val cacheKey = "menu:tree:all"

        cache.get(cacheKey)?.let { cached ->
            logger.debug("Cache hit for menu tree")
            return mapper.readValue(cached)
        }

        val menus = listWithCache()
        val tree = buildMenuTreeForAdmin(menus, 0)

        // 缓存1小时
        cache.set(cacheKey, mapper.writeValueAsString(tree), CacheConstants.DEFAULT_EXPIRE_TIME)

        return tree
    }

    /**
     * 构建管理员菜单树
     */
    private fun buildMenuTreeForAdmin(menus: List<SysMenu>, parentId: Int): List<SysMenu> {
        return menus.filter { it.parentId == parentId }
            .map { menu ->
                menu.apply {
                    children = buildMenuTreeForAdmin(menus, menu.menuId)
                }
            }
    }

    /**
     * 清除用户菜单缓存
     */
    fun clearUserMenuCache(userId: Int) {
        cache.delete("${CacheConstants.USER_MENU_TREE_PREFIX}$userId")
        logger.info("Cleared menu cache for user: userId=$userId")
    }

    /**
     * 清除所有菜单缓存
     */
    fun clearAllMenuCache() {
        cache.deleteByPattern("menu:*")
        cache.deleteByPattern("${CacheConstants.USER_MENU_TREE_PREFIX}*")
        logger.info("Cleared all menu cache")
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
