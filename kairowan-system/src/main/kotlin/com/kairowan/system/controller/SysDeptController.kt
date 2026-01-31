package com.kairowan.system.controller

import com.kairowan.common.KResult
import com.kairowan.core.controller.KController
import com.kairowan.core.framework.security.requirePermission
import com.kairowan.system.api.SystemApiRoutes
import com.kairowan.system.domain.SysDept
import com.kairowan.system.service.SysDeptService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.post
import io.github.smiley4.ktorswaggerui.dsl.put
import io.github.smiley4.ktorswaggerui.dsl.delete
import com.kairowan.core.controller.AuthenticatedRouteController

/**
 * 部门管理控制器
 * @author Kairowan
 * @date 2026-01-18
 */
class SysDeptController : KController(), AuthenticatedRouteController {

    override fun register(route: Route) {
        route.routes()
    }

    private fun Route.routes() {
        val deptService by inject<SysDeptService>()

        route(SystemApiRoutes.Dept.ROOT) {
            // 部门列表
            get(SystemApiRoutes.Dept.LIST, {
                tags = listOf("System Dept")
                summary = "获取部门列表"
                securitySchemeName = "BearerAuth"
            }) {
                val list = deptService.listDepts()
                call.respond(KResult.ok(mapOf("total" to list.size, "rows" to list)))
            }

            // 部门树
            get(SystemApiRoutes.Dept.TREE_SELECT, {
                tags = listOf("System Dept")
                summary = "获取部门下拉树"
                securitySchemeName = "BearerAuth"
            }) {
                val tree = deptService.selectDeptTree()
                call.respond(KResult.ok(tree))
            }

            // 部门详情
            get(SystemApiRoutes.Dept.DETAIL, {
                tags = listOf("System Dept")
                summary = "获取部门详情"
                securitySchemeName = "BearerAuth"
            }) {
                val deptId = call.parameters["deptId"]?.toLongOrNull()
                    ?: return@get call.respond(KResult.fail<Any>("参数错误"))
                val dept = deptService.getById(deptId)
                call.respond(KResult.ok(dept))
            }

            // 新增部门
            requirePermission("system:dept:add") {
                post({
                    tags = listOf("System Dept")
                    summary = "新增部门"
                    securitySchemeName = "BearerAuth"
                    request {
                        body<SysDept> {
                            description = "部门对象"
                            required = true
                        }
                    }
                }) {
                    val dept = call.receive<SysDept>()
                    deptService.save(dept)
                    call.respond(toAjax(true))
                }
            }

            // 修改部门
            requirePermission("system:dept:edit") {
                put({
                    tags = listOf("System Dept")
                    summary = "修改部门"
                    securitySchemeName = "BearerAuth"
                }) {
                    val dept = call.receive<SysDept>()
                    deptService.update(dept)
                    call.respond(toAjax(true))
                }
            }

            // 删除部门
            requirePermission("system:dept:remove") {
                delete(SystemApiRoutes.Dept.DELETE_ONE, {
                    tags = listOf("System Dept")
                    summary = "删除部门"
                    securitySchemeName = "BearerAuth"
                }) {
                    val deptId = call.parameters["deptId"]?.toLongOrNull()
                        ?: return@delete call.respond(KResult.fail<Any>("参数错误"))
                    deptService.deleteById(deptId)
                    call.respond(toAjax(true))
                }
            }
        }
    }
}
