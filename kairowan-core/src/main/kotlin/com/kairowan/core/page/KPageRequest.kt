package com.kairowan.core.page

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

    fun normalized(maxPageSize: Int = 200): KPageRequest {
        val safePageNum = if (pageNum < 1) 1 else pageNum
        val safePageSize = pageSize.coerceIn(1, maxPageSize)
        val safeOrderBy = orderByColumn
            ?.trim()
            ?.takeIf { it.matches(Regex("^[A-Za-z0-9_]+$")) }
        val safeIsAsc = if (isAsc?.lowercase() == "desc") "desc" else "asc"
        return copy(
            pageNum = safePageNum,
            pageSize = safePageSize,
            orderByColumn = safeOrderBy,
            isAsc = safeIsAsc
        )
    }
}
