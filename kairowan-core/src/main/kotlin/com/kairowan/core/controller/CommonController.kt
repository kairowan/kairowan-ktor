package com.kairowan.core.controller

import com.kairowan.common.KResult
import com.kairowan.common.constant.ResultCode
import com.kairowan.core.api.CoreApiRoutes
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.util.*

/**
 * 通用请求处理 (文件上传/下载)
 * Common Controller
 *
 * @author Kairowan
 * @date 2026-01-17
 */
class CommonController : KController(), PublicRouteController {

    override fun register(route: Route) {
        route.routes()
    }

    private fun Route.routes() {
        route(CoreApiRoutes.Common.ROOT) {
            
            // 上传文件
            post(CoreApiRoutes.Common.UPLOAD) {
                val multipart = call.receiveMultipart()
                var fileName = ""
                var url = ""
                
                // 读取配置目录
                val uploadPath = call.application.environment.config.property("kairowan.profile").getString()
                val uploadDir = File(uploadPath)
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs()
                }

                multipart.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        val originalName = part.originalFileName as String
                        val ext = originalName.substringAfterLast(".", "")
                        val uuidName = "${UUID.randomUUID()}.$ext"
                        val file = File(uploadDir, uuidName)
                        
                        part.streamProvider().use { input ->
                            file.outputStream().buffered().use { output ->
                                input.copyTo(output)
                            }
                        }
                        
                        fileName = uuidName
                        url = "/common/download?fileName=$uuidName&delete=false"
                    }
                    part.dispose()
                }
                
                if (fileName.isEmpty()) {
                    call.respond(KResult.fail<Any>("上传失败，未找到文件"))
                } else {
                    val data = mapOf(
                        "fileName" to fileName,
                        "url" to url
                    )
                    call.respond(KResult.ok(data))
                }
            }

            // 下载文件
            get(CoreApiRoutes.Common.DOWNLOAD) {
                val fileName = call.parameters["fileName"]
                if (fileName.isNullOrBlank()) {
                    return@get call.respond(KResult.fail<Any>("文件名不能为空"))
                }

                val uploadPath = call.application.environment.config.property("kairowan.profile").getString()
                val file = File(uploadPath, fileName)

                if (!file.exists()) {
                    call.respond(KResult.fail<Any>(ResultCode.NOT_FOUND))
                } else {
                    call.response.header(
                        HttpHeaders.ContentDisposition,
                        ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, fileName).toString()
                    )
                    call.respondFile(file)
                }
            }
        }
    }
}
