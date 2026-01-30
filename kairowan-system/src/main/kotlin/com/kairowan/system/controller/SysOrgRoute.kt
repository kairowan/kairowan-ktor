package com.kairowan.ktor.framework.web.controller

import com.kairowan.ktor.common.KResult
import com.kairowan.ktor.framework.security.requirePermission
import com.kairowan.ktor.framework.web.domain.SysDept
import com.kairowan.ktor.framework.web.domain.SysPost
import com.kairowan.ktor.framework.web.service.SysDeptService
import com.kairowan.ktor.framework.web.service.SysPostService
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

/**
 * 部门管理控制器
 * @author Kairowan
 * @date 2026-01-18
 */
class SysDeptController : KController() {

    fun Route.routes() {
        val deptService by inject<SysDeptService>()

        route("/system/dept") {
            // 部门列表
            get("/list", {
                tags = listOf("System Dept")
                summary = "获取部门列表"
                securitySchemeName = "BearerAuth"
            }) {
                val list = deptService.listDepts()
                call.respond(KResult.ok(list))
            }

            // 部门树
            get("/treeselect", {
                tags = listOf("System Dept")
                summary = "获取部门下拉树"
                securitySchemeName = "BearerAuth"
            }) {
                val tree = deptService.selectDeptTree()
                call.respond(KResult.ok(tree))
            }

            // 部门详情
            get("/{deptId}", {
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
                delete("/{deptId}", {
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

/**
 * 岗位管理控制器
 * @author Kairowan
 * @date 2026-01-18
 */
class SysPostController : KController() {

    fun Route.routes() {
        val postService by inject<SysPostService>()

        route("/system/post") {
            // 岗位列表
            get("/list", {
                tags = listOf("System Post")
                summary = "获取岗位列表"
                securitySchemeName = "BearerAuth"
            }) {
                val page = getPageRequest(call)
                val list = postService.list(page)
                call.respond(list)
            }

            // 岗位下拉列表
            get("/optionselect", {
                tags = listOf("System Post")
                summary = "获取岗位下拉列表"
                securitySchemeName = "BearerAuth"
            }) {
                val list = postService.selectPostAll()
                call.respond(KResult.ok(list))
            }

            // 岗位详情
            get("/{postId}", {
                tags = listOf("System Post")
                summary = "获取岗位详情"
                securitySchemeName = "BearerAuth"
            }) {
                val postId = call.parameters["postId"]?.toLongOrNull()
                    ?: return@get call.respond(KResult.fail<Any>("参数错误"))
                val post = postService.getById(postId)
                call.respond(KResult.ok(post))
            }

            // 新增岗位
            requirePermission("system:post:add") {
                post({
                    tags = listOf("System Post")
                    summary = "新增岗位"
                    securitySchemeName = "BearerAuth"
                    request {
                        body<SysPost> {
                            description = "岗位对象"
                            required = true
                        }
                    }
                }) {
                    val post = call.receive<SysPost>()
                    postService.save(post)
                    call.respond(toAjax(true))
                }
            }

            // 修改岗位
            requirePermission("system:post:edit") {
                put({
                    tags = listOf("System Post")
                    summary = "修改岗位"
                    securitySchemeName = "BearerAuth"
                }) {
                    val post = call.receive<SysPost>()
                    postService.update(post)
                    call.respond(toAjax(true))
                }
            }

            // 删除岗位
            requirePermission("system:post:remove") {
                delete("/{postId}", {
                    tags = listOf("System Post")
                    summary = "删除岗位"
                    securitySchemeName = "BearerAuth"
                }) {
                    val postId = call.parameters["postId"]?.toLongOrNull()
                        ?: return@delete call.respond(KResult.fail<Any>("参数错误"))
                    postService.deleteById(postId)
                    call.respond(toAjax(true))
                }
            }
        }
    }
}

fun Route.sysDeptRoutes() {
    SysDeptController().apply { routes() }
}

fun Route.sysPostRoutes() {
    SysPostController().apply { routes() }
}
