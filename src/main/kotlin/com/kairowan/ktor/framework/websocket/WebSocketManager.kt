package com.kairowan.ktor.framework.websocket

import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * WebSocket 会话管理器
 * 管理所有连接的 WebSocket 会话
 * 
 * @author Kairowan
 * @date 2026-01-18
 */
object WebSocketManager {
    
    private val logger = LoggerFactory.getLogger(WebSocketManager::class.java)
    private val sessionIdCounter = AtomicLong(0)
    private val mutex = Mutex()
    
    // 所有会话 (sessionId -> session)
    private val sessions = ConcurrentHashMap<Long, WebSocketSessionWrapper>()
    
    // 用户会话映射 (userId -> sessionIds)
    private val userSessions = ConcurrentHashMap<Int, MutableSet<Long>>()
    
    /**
     * 注册会话
     */
    suspend fun register(session: DefaultWebSocketSession, userId: Int? = null): Long {
        val sessionId = sessionIdCounter.incrementAndGet()
        val wrapper = WebSocketSessionWrapper(
            sessionId = sessionId,
            session = session,
            userId = userId
        )
        
        sessions[sessionId] = wrapper
        
        if (userId != null) {
            mutex.withLock {
                userSessions.getOrPut(userId) { mutableSetOf() }.add(sessionId)
            }
        }
        
        logger.info("WebSocket session registered: $sessionId (userId: $userId)")
        return sessionId
    }
    
    /**
     * 注销会话
     */
    suspend fun unregister(sessionId: Long) {
        val wrapper = sessions.remove(sessionId)
        if (wrapper?.userId != null) {
            mutex.withLock {
                userSessions[wrapper.userId]?.remove(sessionId)
                if (userSessions[wrapper.userId]?.isEmpty() == true) {
                    userSessions.remove(wrapper.userId)
                }
            }
        }
        logger.info("WebSocket session unregistered: $sessionId")
    }
    
    /**
     * 发送消息给指定会话
     */
    suspend fun sendTo(sessionId: Long, message: String): Boolean {
        val wrapper = sessions[sessionId] ?: return false
        return try {
            wrapper.session.send(Frame.Text(message))
            true
        } catch (e: Exception) {
            logger.error("Failed to send message to session $sessionId", e)
            false
        }
    }
    
    /**
     * 发送消息给指定用户的所有会话
     */
    suspend fun sendToUser(userId: Int, message: String): Int {
        val sessionIds = userSessions[userId]?.toList() ?: return 0
        var successCount = 0
        for (sessionId in sessionIds) {
            if (sendTo(sessionId, message)) {
                successCount++
            }
        }
        return successCount
    }
    
    /**
     * 广播消息给所有会话
     */
    suspend fun broadcast(message: String): Int {
        var successCount = 0
        for ((sessionId, _) in sessions) {
            if (sendTo(sessionId, message)) {
                successCount++
            }
        }
        logger.info("Broadcast message to $successCount sessions")
        return successCount
    }
    
    /**
     * 获取在线会话数
     */
    fun getOnlineCount(): Int = sessions.size
    
    /**
     * 获取指定用户的在线会话数
     */
    fun getUserSessionCount(userId: Int): Int = userSessions[userId]?.size ?: 0
    
    /**
     * 获取所有在线用户ID
     */
    fun getOnlineUserIds(): Set<Int> = userSessions.keys.toSet()
}

/**
 * WebSocket 会话包装器
 */
data class WebSocketSessionWrapper(
    val sessionId: Long,
    val session: DefaultWebSocketSession,
    val userId: Int?,
    val connectedAt: Long = System.currentTimeMillis()
)
