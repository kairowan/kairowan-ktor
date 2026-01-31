package com.kairowan.system.controller

import com.kairowan.common.KResult
import com.kairowan.core.controller.KController
import com.kairowan.core.framework.security.requirePermission
import com.kairowan.system.api.SystemApiRoutes
import com.kairowan.system.domain.SysPost
import com.kairowan.system.service.SysPostService
import io.github.smiley4.ktorswaggerui.dsl.delete
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.post
import io.github.smiley4.ktorswaggerui.dsl.put
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import com.kairowan.core.controller.AuthenticatedRouteController

/**
 * 岗位管理控制器
 * @author Kairowan
 * @date 2026-01-18
 */
class SysPostController : KController(), AuthenticatedRouteController {

    override fun register(route: Route) {
        route.routes()
    }

    private fun Route.routes() {
        val postService by inject<SysPostService>()

        route(SystemApiRoutes.Post.ROOT) {
            // 岗位列表
            get(SystemApiRoutes.Post.LIST, {
                tags = listOf("System Post")
                summary = "获取岗位列表"
                securitySchemeName = "BearerAuth"
            }) {
                val page = getPageRequest(call)
                val list = postService.list(page)
                call.respond(KResult.ok(mapOf("total" to list.total, "rows" to list.rows)))
            }

            // 岗位下拉列表
            get(SystemApiRoutes.Post.OPTION_SELECT, {
                tags = listOf("System Post")
                summary = "获取岗位下拉列表"
                securitySchemeName = "BearerAuth"
            }) {
                val list = postService.selectPostAll()
                call.respond(KResult.ok(mapOf("total" to list.size, "rows" to list)))
            }

            // 岗位详情
            get(SystemApiRoutes.Post.DETAIL, {
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
                delete(SystemApiRoutes.Post.DELETE_ONE, {
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
