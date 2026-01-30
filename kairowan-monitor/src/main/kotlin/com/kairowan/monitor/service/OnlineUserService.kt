package com.kairowan.ktor.framework.web.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kairowan.ktor.core.cache.CacheProvider
import com.kairowan.ktor.framework.security.LoginUser
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 在线用户服务
 * @author Kairowan
 * @date 2026-01-18
 */
class OnlineUserService(private val cache: CacheProvider) {

    companion object {
        private const val ONLINE_PREFIX = "online_user:"
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
        // 默认 24 小时
        cache.set("$ONLINE_PREFIX$tokenId", json, 86400)
    }

    /**
     * 获取所有在线用户
     */
    fun getOnlineUsers(): List<OnlineUser> {
        val keys = cache.keys("$ONLINE_PREFIX*")
        if (keys.isEmpty()) return emptyList()

        return keys.mapNotNull { key ->
            try {
                val json = cache.get(key) ?: return@mapNotNull null
                mapper.readValue(json, OnlineUser::class.java)
            } catch (e: Exception) {
                null
            }
        }.sortedByDescending { it.loginTime }
    }

    /**
     * 强制踢出用户
     */
    fun forceLogout(tokenId: String): Boolean {
        // 删除在线记录
        cache.delete("$ONLINE_PREFIX$tokenId")
        // 加入 token 黑名单
        cache.set("${TOKEN_PREFIX}blacklist:$tokenId", "1", 86400)
        return true
    }

    /**
     * 用户下线
     */
    fun setOffline(tokenId: String) {
        cache.delete("$ONLINE_PREFIX$tokenId")
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
        return cache.keys("$ONLINE_PREFIX*").size.toLong()
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
