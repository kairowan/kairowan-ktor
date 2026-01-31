package com.kairowan.system.vo

import kotlinx.serialization.Serializable

/**
 * 登录结果
 */
@Serializable
data class LoginResult(
    val token: String
)
