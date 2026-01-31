package com.kairowan.system.vo

import com.kairowan.system.domain.SysRole
import kotlinx.serialization.Serializable

/**
 * 角色视图
 */
@Serializable
data class SysRoleVo(
    val roleId: Int,
    val roleName: String,
    val roleKey: String,
    val roleSort: Int,
    val status: String,
    val createTime: String? = null
)

/**
 * Entity转视图扩展函数
 */
fun SysRole.toVo(): SysRoleVo {
    return SysRoleVo(
        roleId = this.roleId,
        roleName = this.roleName,
        roleKey = this.roleKey,
        roleSort = this.roleSort,
        status = this.status,
        createTime = this.createTime?.toString()
    )
}

/**
 * Entity列表转视图列表
 */
fun List<SysRole>.toVoList(): List<SysRoleVo> {
    return this.map { it.toVo() }
}
