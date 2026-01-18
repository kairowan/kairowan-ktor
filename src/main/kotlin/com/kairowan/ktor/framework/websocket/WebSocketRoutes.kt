package com.kairowan.ktor.framework.websocket

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import org.slf4j.LoggerFactory

/**
 * WebSocket 路由配置
 * @author Kairowan
 * @date 2026-01-18
 */
fun Route.webSocketRoutes() {
    val logger = LoggerFactory.getLogger("WebSocketRoutes")
    val mapper = jacksonObjectMapper()

    // 通用消息通道 (可匿名连接)
    webSocket("/ws") {
        val sessionId = WebSocketManager.register(this)
        
        try {
            // 发送欢迎消息
            send(Frame.Text(mapper.writeValueAsString(
                WsMessage(type = "connected", data = mapOf("sessionId" to sessionId))
            )))
            
            // 接收消息
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        logger.debug("Received: $text")
                        
                        // 解析消息并处理
                        try {
                            val msg = mapper.readValue(text, WsMessage::class.java)
                            handleMessage(sessionId, msg)
                        } catch (e: Exception) {
                            send(Frame.Text(mapper.writeValueAsString(
                                WsMessage(type = "error", data = "Invalid message format")
                            )))
                        }
                    }
                    is Frame.Ping -> send(Frame.Pong(frame.data))
                    else -> {}
                }
            }
        } catch (e: ClosedReceiveChannelException) {
            logger.info("WebSocket closed: $sessionId")
        } catch (e: Exception) {
            logger.error("WebSocket error: $sessionId", e)
        } finally {
            WebSocketManager.unregister(sessionId)
        }
    }

    // 用户专属通道 (需要 userId 参数)
    webSocket("/ws/user/{userId}") {
        val userId = call.parameters["userId"]?.toIntOrNull()
        if (userId == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Missing userId"))
            return@webSocket
        }
        
        val sessionId = WebSocketManager.register(this, userId)
        
        try {
            send(Frame.Text(mapper.writeValueAsString(
                WsMessage(type = "connected", data = mapOf(
                    "sessionId" to sessionId,
                    "userId" to userId
                ))
            )))
            
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        try {
                            val msg = mapper.readValue(text, WsMessage::class.java)
                            handleUserMessage(sessionId, userId, msg)
                        } catch (e: Exception) {
                            send(Frame.Text(mapper.writeValueAsString(
                                WsMessage(type = "error", data = "Invalid message format")
                            )))
                        }
                    }
                    is Frame.Ping -> send(Frame.Pong(frame.data))
                    else -> {}
                }
            }
        } catch (e: ClosedReceiveChannelException) {
            logger.info("User WebSocket closed: $sessionId (userId: $userId)")
        } catch (e: Exception) {
            logger.error("User WebSocket error: $sessionId", e)
        } finally {
            WebSocketManager.unregister(sessionId)
        }
    }
}

/**
 * 处理通用消息
 */
private suspend fun DefaultWebSocketServerSession.handleMessage(sessionId: Long, msg: WsMessage) {
    val mapper = jacksonObjectMapper()
    
    when (msg.type) {
        "ping" -> {
            send(Frame.Text(mapper.writeValueAsString(
                WsMessage(type = "pong", data = System.currentTimeMillis())
            )))
        }
        "broadcast" -> {
            // 广播消息 (仅示例，生产环境需权限控制)
            val content = msg.data?.toString() ?: ""
            WebSocketManager.broadcast(mapper.writeValueAsString(
                WsMessage(type = "broadcast", data = content)
            ))
        }
        else -> {
            send(Frame.Text(mapper.writeValueAsString(
                WsMessage(type = "echo", data = msg.data)
            )))
        }
    }
}

/**
 * 处理用户消息
 */
private suspend fun DefaultWebSocketServerSession.handleUserMessage(sessionId: Long, userId: Int, msg: WsMessage) {
    val mapper = jacksonObjectMapper()
    
    when (msg.type) {
        "ping" -> {
            send(Frame.Text(mapper.writeValueAsString(
                WsMessage(type = "pong", data = System.currentTimeMillis())
            )))
        }
        else -> {
            send(Frame.Text(mapper.writeValueAsString(
                WsMessage(type = "received", data = msg.data)
            )))
        }
    }
}

/**
 * WebSocket 消息格式
 */
data class WsMessage(
    val type: String,
    val data: Any? = null
)
