package com.kairowan.system.controller

import com.kairowan.common.KResult
import com.kairowan.core.controller.KController
import com.kairowan.system.api.SystemApiRoutes
import com.kairowan.system.service.SysUserService
import com.kairowan.system.domain.SysUser
import com.kairowan.system.domain.SysUserExportVo
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import io.github.smiley4.ktorswaggerui.dsl.post
import io.github.smiley4.ktorswaggerui.dsl.get
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.http.*
import com.kairowan.common.utils.ExcelUtils
import com.kairowan.core.controller.AuthenticatedRouteController

/**
 * 系统用户控制器
 * System User Controller
 *
 * @author Kairowan
 * @date 2026-01-17
 */
class SysUserController : KController(), AuthenticatedRouteController {
    
    override fun register(route: Route) {
        route.routes()
    }

    private fun Route.routes() {
        val sysUserService by inject<SysUserService>()
        
        route(SystemApiRoutes.User.ROOT) {
            get(SystemApiRoutes.User.LIST) {
                val page = getPageRequest(call)
                val list = sysUserService.list(page)
                call.respond(KResult.ok(mapOf("total" to list.total, "rows" to list.rows)))
            }
            
            get(SystemApiRoutes.User.DETAIL) {
                val userId = call.parameters["userId"]?.toIntOrNull() 
                    ?: return@get call.respond(KResult.fail<Any>("Invalid ID"))
                val user = sysUserService.getByUserId(userId)
                call.respond(KResult.ok(user))
            }
            
            post({
                tags = listOf("System User")
                summary = "Create System User"
                description = "Creates a new system user"
                request {
                    body<SysUser> {
                        description = "User object"
                        required = true
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Successful Operation"
                        body<KResult<Void>>()
                    }
                }
            }) {
                val user = call.receive<SysUser>()
                if (user.userName.length < 4) {
                    throw RequestValidationException(user, listOf("Username must be at least 4 characters"))
                }

                sysUserService.save(user)
                call.respond(toAjax(true))
            }

            get(SystemApiRoutes.User.EXPORT) {
                val tableData = sysUserService.list()
                val list = tableData.rows as List<SysUser>
                val exportList = list.map { SysUserExportVo(it.userId, it.userName, it.nickName) }
                val bytes = ExcelUtils.exportObjects(exportList, SysUserExportVo::class.java)
                
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "users.xlsx").toString()
                )
                call.respondBytes(bytes)
            }
        }
    }
}
