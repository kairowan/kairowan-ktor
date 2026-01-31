package com.kairowan.system.controller

import com.kairowan.common.KResult
import com.kairowan.core.controller.KController
import com.kairowan.core.framework.security.requirePermission
import com.kairowan.system.api.SystemApiRoutes
import com.kairowan.system.service.SysDictService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import io.github.smiley4.ktorswaggerui.dsl.get
import com.kairowan.core.controller.AuthenticatedRouteController

/**
 * 数据字典控制器
 * @author Kairowan
 * @date 2026-01-18
 */
class SysDictController : KController(), AuthenticatedRouteController {

    override fun register(route: Route) {
        route.routes()
    }

    private fun Route.routes() {
        val dictService by inject<SysDictService>()

        route(SystemApiRoutes.Dict.ROOT) {
            // 字典类型列表
            get(SystemApiRoutes.Dict.TYPE_LIST, {
                tags = listOf("System Dict")
                summary = "获取字典类型列表"
                securitySchemeName = "BearerAuth"
            }) {
                val list = dictService.listDictTypes()
                call.respond(KResult.ok(mapOf("total" to list.size, "rows" to list)))
            }

            // 根据字典类型获取字典数据
            get(SystemApiRoutes.Dict.DATA_BY_TYPE, {
                tags = listOf("System Dict")
                summary = "根据字典类型获取字典数据"
                securitySchemeName = "BearerAuth"
            }) {
                val dictType = call.parameters["dictType"] ?: return@get call.respond(KResult.fail<Any>("dictType不能为空"))
                val list = dictService.getDictDataByType(dictType)
                call.respond(KResult.ok(mapOf("total" to list.size, "rows" to list)))
            }

            // 刷新字典缓存
            requirePermission("system:dict:remove") {
                get(SystemApiRoutes.Dict.REFRESH_CACHE, {
                    tags = listOf("System Dict")
                    summary = "刷新字典缓存"
                    securitySchemeName = "BearerAuth"
                }) {
                    val count = dictService.refreshAllCache()
                    call.respond(KResult.ok<Any>(msg = "字典缓存刷新成功", data = mapOf("count" to count)))
                }
            }
        }
    }
}
