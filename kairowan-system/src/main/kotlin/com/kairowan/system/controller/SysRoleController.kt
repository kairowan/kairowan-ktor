package com.kairowan.system.controller

import com.kairowan.common.KResult
import com.kairowan.core.controller.KController
import com.kairowan.core.framework.security.requirePermission
import com.kairowan.system.api.SystemApiRoutes
import com.kairowan.system.domain.SysRole
import com.kairowan.system.service.SysRoleService
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
 * 角色管理控制器
 * @author Kairowan
 * @date 2026-01-18
 */
class SysRoleController : KController(), AuthenticatedRouteController {

    override fun register(route: Route) {
        route.routes()
    }

    private fun Route.routes() {
        val roleService by inject<SysRoleService>()

        route(SystemApiRoutes.Role.ROOT) {
            // 角色列表
            get(SystemApiRoutes.Role.LIST, {
                tags = listOf("System Role")
                summary = "获取角色列表"
                securitySchemeName = "BearerAuth"
            }) {
                val page = getPageRequest(call)
                val list = roleService.list(page)
                call.respond(KResult.ok(mapOf("total" to list.total, "rows" to list.rows)))
            }

            // 新增角色
            requirePermission("system:role:add") {
                post({
                    tags = listOf("System Role")
                    summary = "新增角色"
                    securitySchemeName = "BearerAuth"
                    request {
                        body<SysRole> {
                            description = "角色对象"
                            required = true
                        }
                    }
                }) {
                    val role = call.receive<SysRole>()
                    roleService.save(role)
                    call.respond(toAjax(true))
                }
            }

            // 删除角色 (占位)
            requirePermission("system:role:remove") {
                delete(SystemApiRoutes.Role.DELETE_ONE, {
                    tags = listOf("System Role")
                    summary = "删除角色"
                    securitySchemeName = "BearerAuth"
                }) {
                    val roleId = call.parameters["roleId"]?.toIntOrNull()
                        ?: return@delete call.respond(KResult.fail<Any>("参数错误"))
                    roleService.deleteById(roleId)
                    roleService.clearRoleCache(roleId)
                    call.respond(KResult.ok<Any>(msg = "删除成功"))
                }
            }
        }
    }
}
