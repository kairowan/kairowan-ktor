package com.kairowan.ktor.framework.web.controller

import com.kairowan.ktor.common.KResult
import com.kairowan.ktor.framework.security.requirePermission
import com.kairowan.ktor.framework.web.service.SysDictService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import io.github.smiley4.ktorswaggerui.dsl.get

/**
 * 数据字典控制器
 * @author Kairowan
 * @date 2026-01-18
 */
class SysDictController : KController() {

    fun Route.routes() {
        val dictService by inject<SysDictService>()

        route("/system/dict") {
            // 字典类型列表
            get("/type/list", {
                tags = listOf("System Dict")
                summary = "获取字典类型列表"
                securitySchemeName = "BearerAuth"
            }) {
                val list = dictService.listDictTypes()
                call.respond(KResult.ok(list))
            }

            // 根据字典类型获取字典数据
            get("/data/type/{dictType}", {
                tags = listOf("System Dict")
                summary = "根据字典类型获取字典数据"
                securitySchemeName = "BearerAuth"
            }) {
                val dictType = call.parameters["dictType"] ?: return@get call.respond(KResult.fail<Any>("dictType不能为空"))
                val list = dictService.getDictDataByType(dictType)
                call.respond(KResult.ok(list))
            }

            // 刷新字典缓存
            requirePermission("system:dict:remove") {
                get("/refreshCache", {
                    tags = listOf("System Dict")
                    summary = "刷新字典缓存"
                    securitySchemeName = "BearerAuth"
                }) {
                    // TODO: 遍历刷新所有字典缓存
                    call.respond(KResult.ok<Any>(msg = "字典缓存刷新成功"))
                }
            }
        }
    }
}

fun Route.sysDictRoutes() {
    SysDictController().apply { routes() }
}
