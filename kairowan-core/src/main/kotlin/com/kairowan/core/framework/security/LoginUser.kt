package com.kairowan.core.framework.security

import io.ktor.server.auth.*

/**
 * 登录用户信息
 * 用于 JWT 认证后的用户身份
 */
data class LoginUser(
    val userId: Int,
    val username: String,
    val user: Any? = null,
    val roles: Set<String> = emptySet(),
    val permissions: Set<String> = emptySet()
) : Principal
