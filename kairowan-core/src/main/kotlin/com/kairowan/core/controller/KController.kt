package com.kairowan.core.controller

import com.kairowan.common.KResult
import com.kairowan.core.page.KPageRequest
import com.kairowan.core.page.KTableData
import io.ktor.server.application.*

import com.kairowan.core.framework.security.LoginUser
import io.ktor.server.auth.*

/**
 * 通用控制器基类
 * Kairowan Base Web Controller
 *
 * @author Kairowan
 * @date 2026-01-17
 */
open class KController {

    /**
     * 获取当前登录用户
     */
    protected fun getLoginUser(call: ApplicationCall): LoginUser? {
        return call.principal<LoginUser>()
    }
    
    protected fun getUserId(call: ApplicationCall): Int? {
        return getLoginUser(call)?.userId
    }

    /**
     * 获取分页 request
     */
    protected fun getPageRequest(call: ApplicationCall): KPageRequest {
        // 优先从 queryParameters 读取（GET 请求），然后从 parameters 读取（路径参数）
        val queryParams = call.request.queryParameters
        val pageNum = queryParams["pageNum"]?.toIntOrNull()
            ?: call.parameters["pageNum"]?.toIntOrNull()
            ?: 1
        val pageSize = queryParams["pageSize"]?.toIntOrNull()
            ?: call.parameters["pageSize"]?.toIntOrNull()
            ?: 10
        val orderBy = queryParams["orderByColumn"] ?: call.parameters["orderByColumn"]
        val isAsc = queryParams["isAsc"] ?: call.parameters["isAsc"]

        return KPageRequest(pageNum, pageSize, orderBy, isAsc).normalized()
    }

    /**
     * 响应请求表格数据
     */
    protected fun getDataTable(list: List<Any>, total: Long): KTableData {
        return KTableData.build(list, total)
    }

    /**
     * 响应返回结果
     */
    protected fun toAjax(rows: Int): KResult<Void> {
        return if (rows > 0) KResult.ok() else KResult.fail("操作失败")
    }
    
    protected fun toAjax(result: Boolean): KResult<Void> {
        return if (result) KResult.ok() else KResult.fail("操作失败")
    }
}
