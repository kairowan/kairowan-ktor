package com.kairowan.core.req

/**
 * 登录请求
 */
data class LoginReq(
    val username: String = "",
    val password: String = "",
    val code: String? = null,
    val uuid: String? = null
)
