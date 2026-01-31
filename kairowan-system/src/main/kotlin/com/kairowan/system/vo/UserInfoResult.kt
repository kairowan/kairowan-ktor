package com.kairowan.system.vo

import kotlinx.serialization.Serializable

/**
 * 用户信息结果（包含权限和角色）
 */
@Serializable
data class UserInfoResult(
    val user: UserInfo,
    val roles: Set<String>,
    val permissions: Set<String>
)
