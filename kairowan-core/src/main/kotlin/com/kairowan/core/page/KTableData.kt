package com.kairowan.core.page

import java.io.Serializable

/**
 * 表格数据对象
 * Kairowan Grid Data Wrapper
 *
 * @author Kairowan
 * @date 2026-01-17
 */
data class KTableData(
    val total: Long,
    val rows: List<Any>,
    val code: Int = 200,
    val msg: String = "查询成功"
) : Serializable {
    companion object {
        fun build(list: List<Any>, total: Long = list.size.toLong()): KTableData {
            return KTableData(total, list)
        }
    }
}
