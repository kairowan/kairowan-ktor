package com.kairowan.ktor.framework.web.controller

import com.kairowan.ktor.common.KResult
import com.kairowan.ktor.framework.security.requirePermission
import com.kairowan.ktor.framework.web.service.SysLoginLogService
import com.kairowan.ktor.framework.web.service.SysOperLogService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.delete

/**
 * 日志管理控制器
 * @author Kairowan
 * @date 2026-01-18
 */
class SysLogController : KController() {

    fun Route.routes() {
        val operLogService by inject<SysOperLogService>()
        val loginLogService by inject<SysLoginLogService>()

        route("/monitor") {
            // 操作日志列表
            get("/operlog/list", {
                tags = listOf("Monitor Log")
                summary = "查询操作日志列表"
                securitySchemeName = "BearerAuth"
            }) {
                val page = getPageRequest(call)
                val list = operLogService.list(page)
                call.respond(list)
            }

            // 清空操作日志
            requirePermission("monitor:operlog:remove") {
                delete("/operlog/clean", {
                    tags = listOf("Monitor Log")
                    summary = "清空操作日志"
                    securitySchemeName = "BearerAuth"
                }) {
                    operLogService.clean()
                    call.respond(KResult.ok<Any>(msg = "清空成功"))
                }
            }

            // 登录日志列表
            get("/logininfor/list", {
                tags = listOf("Monitor Log")
                summary = "查询登录日志列表"
                securitySchemeName = "BearerAuth"
            }) {
                val page = getPageRequest(call)
                val list = loginLogService.list(page)
                call.respond(list)
            }

            // 清空登录日志
            requirePermission("monitor:logininfor:remove") {
                delete("/logininfor/clean", {
                    tags = listOf("Monitor Log")
                    summary = "清空登录日志"
                    securitySchemeName = "BearerAuth"
                }) {
                    loginLogService.clean()
                    call.respond(KResult.ok<Any>(msg = "清空成功"))
                }
            }
        }
    }
}

fun Route.sysLogRoutes() {
    SysLogController().apply { routes() }
}
