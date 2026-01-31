package com.kairowan.core.req

/**
 * 批量 ID 请求
 */
data class IdsReq(
    val ids: List<Long> = emptyList()
)
