package com.kairowan.ktor.framework.security

import com.kairowan.ktor.framework.web.domain.SysUser
import io.ktor.server.auth.*

/**
 * 登录用户身份信息
 * @author Kairowan
 * @date 2026-01-17
 */
data class LoginUser(
    val userId: Int,
    val username: String,
    val user: SysUser? = null,
    val roles: Set<String> = emptySet(),
    val permissions: Set<String> = emptySet()
) : Principal

