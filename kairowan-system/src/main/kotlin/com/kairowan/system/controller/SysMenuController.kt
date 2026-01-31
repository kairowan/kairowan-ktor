package com.kairowan.system.controller

import com.kairowan.common.KResult
import com.kairowan.core.controller.KController
import com.kairowan.core.framework.security.requirePermission
import com.kairowan.system.api.SystemApiRoutes
import com.kairowan.system.domain.SysMenu
import com.kairowan.system.service.SysMenuService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.post
import io.github.smiley4.ktorswaggerui.dsl.delete
import com.kairowan.core.controller.AuthenticatedRouteController

/**
 * 菜单管理控制器
 * @author Kairowan
 * @date 2026-01-18
 */
class SysMenuController : KController(), AuthenticatedRouteController {

    override fun register(route: Route) {
        route.routes()
    }

    private fun Route.routes() {
        val menuService by inject<SysMenuService>()

        route(SystemApiRoutes.Menu.ROOT) {
            // 菜单列表
            get(SystemApiRoutes.Menu.LIST, {
                tags = listOf("System Menu")
                summary = "获取菜单列表"
                securitySchemeName = "BearerAuth"
            }) {
                val list = menuService.list()
                call.respond(KResult.ok(mapOf("total" to list.total, "rows" to list.rows)))
            }

            // 新增菜单
            requirePermission("system:menu:add") {
                post({
                    tags = listOf("System Menu")
                    summary = "新增菜单"
                    securitySchemeName = "BearerAuth"
                    request {
                        body<SysMenu> {
                            description = "菜单对象"
                            required = true
                        }
                    }
                }) {
                    val menu = call.receive<SysMenu>()
                    menuService.save(menu)
                    call.respond(toAjax(true))
                }
            }

            // 删除菜单 (占位)
            requirePermission("system:menu:remove") {
                delete(SystemApiRoutes.Menu.DELETE_ONE, {
                    tags = listOf("System Menu")
                    summary = "删除菜单"
                    securitySchemeName = "BearerAuth"
                }) {
                    val menuId = call.parameters["menuId"]?.toIntOrNull()
                        ?: return@delete call.respond(KResult.fail<Any>("参数错误"))
                    menuService.deleteById(menuId)
                    menuService.clearAllMenuCache()
                    call.respond(KResult.ok<Any>(msg = "删除成功"))
                }
            }
        }
    }
}
