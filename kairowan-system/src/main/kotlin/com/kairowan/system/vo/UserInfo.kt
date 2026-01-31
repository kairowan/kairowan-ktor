package com.kairowan.system.vo

import kotlinx.serialization.Serializable

/**
 * 用户信息
 */
@Serializable
data class UserInfo(
    val userId: Long,
    val userName: String,
    val nickName: String,
    val email: String? = null,
    val phone: String? = null,
    val sex: String? = null,
    val avatar: String? = null,
    val deptId: Long? = null,
    val deptName: String? = null,
    val roles: List<String> = emptyList(),
    val permissions: List<String> = emptyList()
)
