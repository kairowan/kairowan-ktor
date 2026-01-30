package com.kairowan.ktor.framework.web.controller

import com.kairowan.ktor.common.KResult
import com.kairowan.ktor.framework.security.requirePermission
import com.kairowan.ktor.framework.web.domain.SysMenu
import com.kairowan.ktor.framework.web.service.SysMenuService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.post
import io.github.smiley4.ktorswaggerui.dsl.delete

/**
 * 菜单管理控制器
 * @author Kairowan
 * @date 2026-01-18
 */
class SysMenuController : KController() {

    fun Route.routes() {
        val menuService by inject<SysMenuService>()

        route("/system/menu") {
            // 菜单列表
            get("/list", {
                tags = listOf("System Menu")
                summary = "获取菜单列表"
                securitySchemeName = "BearerAuth"
            }) {
                val list = menuService.list()
                call.respond(list)
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
                delete("/{menuId}", {
                    tags = listOf("System Menu")
                    summary = "删除菜单"
                    securitySchemeName = "BearerAuth"
                }) {
                    // TODO: 实现删除逻辑
                    call.respond(KResult.ok<Any>(msg = "删除成功"))
                }
            }
        }
    }
}

fun Route.sysMenuRoutes() {
    SysMenuController().apply { routes() }
}
