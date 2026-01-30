package com.kairowan.ktor.framework.web.controller

import com.kairowan.ktor.common.KResult
import com.kairowan.ktor.framework.security.requirePermission
import com.kairowan.ktor.framework.web.domain.SysConfig
import com.kairowan.ktor.framework.web.service.SysConfigService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.post
import io.github.smiley4.ktorswaggerui.dsl.put

/**
 * 系统配置控制器
 * @author Kairowan
 * @date 2026-01-18
 */
class SysConfigController : KController() {

    fun Route.routes() {
        val configService by inject<SysConfigService>()

        route("/system/config") {
            // 配置列表
            get("/list", {
                tags = listOf("System Config")
                summary = "获取配置列表"
                securitySchemeName = "BearerAuth"
            }) {
                val page = getPageRequest(call)
                val list = configService.list(page)
                call.respond(list)
            }

            // 根据键名获取配置值
            get("/configKey/{configKey}", {
                tags = listOf("System Config")
                summary = "根据键名获取配置值"
                securitySchemeName = "BearerAuth"
            }) {
                val configKey = call.parameters["configKey"] ?: return@get call.respond(KResult.fail<Any>("configKey不能为空"))
                val value = configService.getConfigValue(configKey)
                call.respond(KResult.ok(value))
            }

            // 新增配置
            requirePermission("system:config:add") {
                post({
                    tags = listOf("System Config")
                    summary = "新增配置"
                    securitySchemeName = "BearerAuth"
                    request {
                        body<SysConfig> {
                            description = "配置对象"
                            required = true
                        }
                    }
                }) {
                    val config = call.receive<SysConfig>()
                    configService.save(config)
                    call.respond(toAjax(true))
                }
            }

            // 刷新缓存
            requirePermission("system:config:remove") {
                get("/refreshCache", {
                    tags = listOf("System Config")
                    summary = "刷新配置缓存"
                    securitySchemeName = "BearerAuth"
                }) {
                    // TODO: 遍历刷新所有缓存
                    call.respond(KResult.ok<Any>(msg = "缓存刷新成功"))
                }
            }
        }
    }
}

fun Route.sysConfigRoutes() {
    SysConfigController().apply { routes() }
}
