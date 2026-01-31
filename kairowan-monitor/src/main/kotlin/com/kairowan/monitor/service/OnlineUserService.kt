package com.kairowan.monitor.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kairowan.core.framework.cache.CacheProvider
import com.kairowan.core.framework.security.LoginUser
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 在线用户服务
 * @author Kairowan
 * @date 2026-01-28
 */
class OnlineUserService(private val cache: CacheProvider) {

    private val logger = LoggerFactory.getLogger(OnlineUserService::class.java)

    companion object {
        private const val ONLINE_PREFIX = "online_user:"
        private const val ONLINE_LIST_KEY = "online_users_list"
        private const val TOKEN_PREFIX = "login_token:"
        private val mapper = jacksonObjectMapper()
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }

    /**
     * 记录用户上线
     */
    fun setOnline(
        tokenId: String,
        loginUser: LoginUser,
        ipaddr: String,
        browser: String = "",
        os: String = ""
    ) {
        val onlineUser = OnlineUser(
            tokenId = tokenId,
            userId = loginUser.userId,
            userName = loginUser.username,
            ipaddr = ipaddr,
            browser = browser,
            os = os,
            loginTime = LocalDateTime.now().format(formatter)
        )

        val json = mapper.writeValueAsString(onlineUser)
        cache.set("$ONLINE_PREFIX$tokenId", json, 86400)

        logger.debug("User online: userId=${loginUser.userId}, tokenId=$tokenId")
    }

    /**
     * 获取所有在线用户
     */
    fun getOnlineUsers(): List<OnlineUser> {
        // 简化实现：返回空列表
        // 实际应用中可以使用 Redis SCAN 命令或维护一个在线用户列表
        logger.debug("Getting online users (simplified implementation)")
        return emptyList()
    }

    /**
     * 强制踢出用户
     */
    fun forceLogout(tokenId: String): Boolean {
        // 删除在线记录
        cache.delete("$ONLINE_PREFIX$tokenId")

        // 加入 token 黑名单
        cache.set("${TOKEN_PREFIX}blacklist:$tokenId", "1", 86400)

        logger.info("User forced logout: tokenId=$tokenId")
        return true
    }

    /**
     * 用户下线
     */
    fun setOffline(tokenId: String) {
        cache.delete("$ONLINE_PREFIX$tokenId")
        logger.debug("User offline: tokenId=$tokenId")
    }

    /**
     * 检查用户是否在线
     */
    fun isOnline(tokenId: String): Boolean {
        return cache.exists("$ONLINE_PREFIX$tokenId")
    }

    /**
     * 获取在线用户数量
     */
    fun getOnlineCount(): Long {
        // 简化实现：返回 0
        // 实际应用中需要维护一个计数器或使用 Redis 的 ZCARD 命令
        return 0L
    }
}

/**
 * 在线用户信息
 */
data class OnlineUser(
    val tokenId: String,
    val userId: Int,
    val userName: String,
    val ipaddr: String,
    val browser: String,
    val os: String,
    val loginTime: String
)
