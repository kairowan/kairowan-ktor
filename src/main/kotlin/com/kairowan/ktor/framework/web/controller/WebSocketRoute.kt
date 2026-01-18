package com.kairowan.ktor.framework.web.controller

import com.kairowan.ktor.common.KResult
import com.kairowan.ktor.framework.websocket.WebSocketManager
import com.kairowan.ktor.framework.websocket.WsMessage
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.github.smiley4.ktorswaggerui.dsl.*

/**
 * WebSocket 消息推送控制器
 * 通过 HTTP API 向 WebSocket 客户端推送消息
 * 
 * @author Kairowan
 * @date 2026-01-18
 */
class WebSocketController : KController() {

    private val mapper = jacksonObjectMapper()

    fun Route.routes() {
        route("/ws/api") {
            // 获取在线连接数
            get("/online/count", {
                tags = listOf("WebSocket")
                summary = "获取 WebSocket 在线连接数"
                securitySchemeName = "BearerAuth"
            }) {
                val count = WebSocketManager.getOnlineCount()
                call.respond(KResult.ok(mapOf("count" to count)))
            }

            // 获取在线用户列表
            get("/online/users", {
                tags = listOf("WebSocket")
                summary = "获取 WebSocket 在线用户ID列表"
                securitySchemeName = "BearerAuth"
            }) {
                val userIds = WebSocketManager.getOnlineUserIds()
                call.respond(KResult.ok(userIds))
            }

            // 发送消息给指定用户
            post("/send/user/{userId}", {
                tags = listOf("WebSocket")
                summary = "发送消息给指定用户"
                securitySchemeName = "BearerAuth"
            }) {
                val userId = call.parameters["userId"]?.toIntOrNull()
                    ?: return@post call.respond(KResult.fail<Any>("userId 参数错误"))
                
                val body = call.receive<MessageBody>()
                val message = mapper.writeValueAsString(
                    WsMessage(type = body.type, data = body.data)
                )
                
                val count = WebSocketManager.sendToUser(userId, message)
                call.respond(KResult.ok(mapOf(
                    "sentTo" to count,
                    "message" to "已发送到 $count 个会话"
                )))
            }

            // 广播消息给所有用户
            post("/broadcast", {
                tags = listOf("WebSocket")
                summary = "广播消息给所有连接"
                securitySchemeName = "BearerAuth"
            }) {
                val body = call.receive<MessageBody>()
                val message = mapper.writeValueAsString(
                    WsMessage(type = body.type, data = body.data)
                )
                
                val count = WebSocketManager.broadcast(message)
                call.respond(KResult.ok(mapOf(
                    "sentTo" to count,
                    "message" to "已广播到 $count 个会话"
                )))
            }

            // 发送消息给指定会话
            post("/send/session/{sessionId}", {
                tags = listOf("WebSocket")
                summary = "发送消息给指定会话"
                securitySchemeName = "BearerAuth"
            }) {
                val sessionId = call.parameters["sessionId"]?.toLongOrNull()
                    ?: return@post call.respond(KResult.fail<Any>("sessionId 参数错误"))
                
                val body = call.receive<MessageBody>()
                val message = mapper.writeValueAsString(
                    WsMessage(type = body.type, data = body.data)
                )
                
                val success = WebSocketManager.sendTo(sessionId, message)
                if (success) {
                    call.respond(KResult.ok<Any>(msg = "发送成功"))
                } else {
                    call.respond(KResult.fail<Any>("会话不存在或已断开"))
                }
            }
        }
    }
}

/**
 * 消息体
 */
data class MessageBody(
    val type: String = "message",
    val data: Any? = null
)

fun Route.webSocketApiRoutes() {
    WebSocketController().apply { routes() }
}
