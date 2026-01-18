package com.kairowan.ktor.framework.web.page

/**
 * 分页查询对象
 * Kairowan Pagination Request Context
 *
 * @author Kairowan
 * @date 2026-01-17
 */
data class KPageRequest(
    var pageNum: Int = 1,
    var pageSize: Int = 10,
    var orderByColumn: String? = null,
    var isAsc: String? = "asc"
) {
    fun getOffset(): Int {
        return (pageNum - 1) * pageSize
    }
}
