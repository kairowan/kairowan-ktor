package com.kairowan.ktor.framework.web.dto

/**
 * 登录请求体
 * @author Kairowan
 * @date 2026-01-18
 */
data class LoginBody(
    val username: String,
    val password: String,
    val code: String? = null,      // 验证码 (可选)
    val uuid: String? = null       // 验证码UUID (可选)
)

/**
 * 登录响应
 */
data class LoginResult(
    val token: String
)

/**
 * 用户信息响应 (getInfo 接口)
 */
data class UserInfoResult(
    val user: UserInfo,
    val roles: Set<String>,
    val permissions: Set<String>
)

data class UserInfo(
    val userId: Int,
    val userName: String,
    val nickName: String,
    val email: String,
    val phone: String,
    val deptId: Int?
)
