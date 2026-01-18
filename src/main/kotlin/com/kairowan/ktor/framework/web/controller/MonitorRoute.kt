package com.kairowan.ktor.framework.web.controller

import com.kairowan.ktor.common.KResult
import com.kairowan.ktor.framework.security.requirePermission
import com.kairowan.ktor.framework.web.service.OnlineUserService
import com.kairowan.ktor.framework.web.service.ServerMonitorService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.delete

/**
 * 监控管理控制器
 * @author Kairowan
 * @date 2026-01-18
 */
class MonitorController : KController() {

    fun Route.routes() {
        val onlineUserService by inject<OnlineUserService>()
        val serverMonitorService by inject<ServerMonitorService>()

        route("/monitor") {
            // ==================== 在线用户 ====================
            
            // 在线用户列表
            get("/online/list", {
                tags = listOf("Monitor - Online")
                summary = "获取在线用户列表"
                securitySchemeName = "BearerAuth"
            }) {
                val users = onlineUserService.getOnlineUsers()
                call.respond(KResult.ok(users))
            }

            // 在线用户数量
            get("/online/count", {
                tags = listOf("Monitor - Online")
                summary = "获取在线用户数量"
                securitySchemeName = "BearerAuth"
            }) {
                val count = onlineUserService.getOnlineCount()
                call.respond(KResult.ok(count))
            }

            // 强制踢出用户
            requirePermission("monitor:online:forceLogout") {
                delete("/online/{tokenId}", {
                    tags = listOf("Monitor - Online")
                    summary = "强制踢出用户"
                    securitySchemeName = "BearerAuth"
                }) {
                    val tokenId = call.parameters["tokenId"]
                        ?: return@delete call.respond(KResult.fail<Any>("tokenId不能为空"))
                    
                    onlineUserService.forceLogout(tokenId)
                    call.respond(KResult.ok<Any>(msg = "踢出成功"))
                }
            }

            // ==================== 服务器监控 ====================
            
            // 服务器信息
            get("/server", {
                tags = listOf("Monitor - Server")
                summary = "获取服务器监控信息"
                securitySchemeName = "BearerAuth"
            }) {
                val serverInfo = serverMonitorService.getServerInfo()
                call.respond(KResult.ok(serverInfo))
            }
        }
    }
}

fun Route.monitorRoutes() {
    MonitorController().apply { routes() }
}
