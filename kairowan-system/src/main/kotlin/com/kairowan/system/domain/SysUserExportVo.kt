package com.kairowan.system.domain

import com.kairowan.common.utils.Excel

/**
 * System User Export VO
 */
data class SysUserExportVo(
    @Excel(name = "User ID")
    val userId: Int,
    
    @Excel(name = "Username")
    val userName: String,
    
    @Excel(name = "Nickname")
    val nickName: String
)
