package com.kairowan.ktor.framework.web.controller

import com.kairowan.ktor.common.KResult
import com.kairowan.ktor.framework.web.page.KPageRequest
import com.kairowan.ktor.framework.web.page.KTableData
import io.ktor.server.application.*

import com.kairowan.ktor.framework.security.LoginUser
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
        val pageNum = call.parameters["pageNum"]?.toIntOrNull() ?: 1
        val pageSize = call.parameters["pageSize"]?.toIntOrNull() ?: 10
        val orderBy = call.parameters["orderByColumn"]
        val isAsc = call.parameters["isAsc"]
        
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
