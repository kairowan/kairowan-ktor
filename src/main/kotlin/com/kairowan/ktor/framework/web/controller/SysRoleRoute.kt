package com.kairowan.ktor.framework.web.controller

import com.kairowan.ktor.common.KResult
import com.kairowan.ktor.framework.security.requirePermission
import com.kairowan.ktor.framework.web.domain.SysRole
import com.kairowan.ktor.framework.web.service.SysRoleService
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
 * 角色管理控制器
 * @author Kairowan
 * @date 2026-01-18
 */
class SysRoleController : KController() {

    fun Route.routes() {
        val roleService by inject<SysRoleService>()

        route("/system/role") {
            // 角色列表
            get("/list", {
                tags = listOf("System Role")
                summary = "获取角色列表"
                securitySchemeName = "BearerAuth"
            }) {
                val page = getPageRequest(call)
                val list = roleService.list(page)
                call.respond(list)
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
                delete("/{roleId}", {
                    tags = listOf("System Role")
                    summary = "删除角色"
                    securitySchemeName = "BearerAuth"
                }) {
                    // TODO: 实现删除逻辑
                    call.respond(KResult.ok<Any>(msg = "删除成功"))
                }
            }
        }
    }
}

fun Route.sysRoleRoutes() {
    SysRoleController().apply { routes() }
}
