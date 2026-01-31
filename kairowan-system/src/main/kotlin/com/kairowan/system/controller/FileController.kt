package com.kairowan.system.controller

import com.kairowan.common.KResult
import com.kairowan.core.controller.KController
import com.kairowan.system.api.SystemApiRoutes
import com.kairowan.common.utils.FileUploadUtils
import com.kairowan.core.req.IdsReq
import io.ktor.http.content.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.kairowan.core.framework.security.LoginUser
import com.kairowan.system.service.FileService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import com.kairowan.core.controller.AuthenticatedRouteController

/**
 * 文件管理控制器
 * @author Kairowan
 * @date 2026-01-29
 */
class FileController : KController(), AuthenticatedRouteController {

    override fun register(route: Route) {
        route.routes()
    }

    private fun Route.routes() {
        val fileService by inject<FileService>()

        route(SystemApiRoutes.File.ROOT) {
            get(SystemApiRoutes.File.LIST) {
                val type = call.request.queryParameters["type"]
                val keyword = call.request.queryParameters["keyword"]
                val page = getPageRequest(call)

                val list = fileService.getFileList(type, keyword, page)
                call.respond(KResult.ok(mapOf("total" to list.total, "rows" to list.rows)))
            }

            get(SystemApiRoutes.File.STATS) {
                val stats = fileService.getStats()
                call.respond(KResult.ok(stats))
            }

            // 上传文件
            post(SystemApiRoutes.File.UPLOAD) {
                val loginUser = call.principal<LoginUser>()
                    ?: return@post call.respond(KResult.fail<Any>("未登录"))

                val multipart = call.receiveMultipart()
                val uploadedFiles = mutableListOf<Map<String, Any?>>()
                var errorMessage: String? = null

                val uploadPath = call.application.environment.config.propertyOrNull("file.uploadPath")?.getString()
                    ?: "uploads"
                val fileUrlPrefix = call.application.environment.config.propertyOrNull("file.urlPrefix")?.getString()
                    ?: "http://localhost:8080/files"

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            if (errorMessage == null) {
                                val fileName = part.originalFileName ?: "unknown"
                                val fileBytes = part.streamProvider().readBytes()
                                val fileSize = fileBytes.size.toLong()

                                val maxSize = 50 * 1024 * 1024L
                                if (!FileUploadUtils.validateFileSize(fileSize, maxSize)) {
                                    errorMessage = "文件大小超过限制（50MB）"
                                } else {
                                    val relativePath = FileUploadUtils.saveFile(
                                        fileBytes.inputStream(),
                                        fileName,
                                        uploadPath
                                    )

                                    val fileType = FileUploadUtils.getFileType(fileName)
                                    val fileUrl = "$fileUrlPrefix/$relativePath"

                                    val fileId = fileService.uploadFile(
                                        fileName = fileName,
                                        fileType = fileType,
                                        fileSize = fileSize,
                                        fileUrl = fileUrl,
                                        thumbnailUrl = null,
                                        uploaderId = loginUser.userId,
                                        uploaderName = loginUser.username
                                    )

                                    uploadedFiles.add(mapOf(
                                        "id" to fileId,
                                        "name" to fileName,
                                        "url" to fileUrl,
                                        "size" to FileUploadUtils.formatFileSize(fileSize),
                                        "type" to FileUploadUtils.getFileExtension(fileName),
                                        "uploadTime" to LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                                        "uploader" to loginUser.username
                                    ))
                                }
                            }
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                when {
                    errorMessage != null -> call.respond(KResult.fail<Any>(errorMessage!!))
                    uploadedFiles.isEmpty() -> call.respond(KResult.fail<Any>("没有文件被上传"))
                    else -> call.respond(KResult.ok("上传成功", uploadedFiles))
                }
            }

            // 下载文件
            get(SystemApiRoutes.File.DOWNLOAD) {
                val fileId = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, KResult.fail<Any>("参数错误"))

                val file = fileService.getFileById(fileId)
                    ?: return@get call.respond(HttpStatusCode.NotFound, KResult.fail<Any>("文件不存在"))

                val uploadPath = call.application.environment.config.propertyOrNull("file.uploadPath")?.getString()
                    ?: "uploads"
                val fileUrlPrefix = call.application.environment.config.propertyOrNull("file.urlPrefix")?.getString()
                    ?: "http://localhost:8080/files"
                val relativePath = file.fileUrl.removePrefix("$fileUrlPrefix/")

                val filePath = FileUploadUtils.getFullPath(relativePath, uploadPath)
                val javaFile = filePath.toFile()

                if (!javaFile.exists()) {
                    return@get call.respond(HttpStatusCode.NotFound, KResult.fail<Any>("文件不存在"))
                }

                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(
                        ContentDisposition.Parameters.FileName,
                        file.fileName
                    ).toString()
                )
                call.respondFile(javaFile)
            }

            // 删除文件
            delete(SystemApiRoutes.File.DELETE_ONE) {
                val fileId = call.parameters["id"]?.toLongOrNull()
                    ?: return@delete call.respond(KResult.fail<Any>("参数错误"))

                fileService.deleteFile(fileId)
                call.respond(KResult.ok<Any>(msg = "删除成功"))
            }

            // 批量删除文件
            delete(SystemApiRoutes.File.DELETE_BATCH) {
                val params = call.receive<IdsReq>()
                val ids = params.ids
                if (ids.isEmpty()) {
                    return@delete call.respond(KResult.fail<Any>("参数错误"))
                }

                fileService.batchDeleteFiles(ids)
                call.respond(KResult.ok<Any>(msg = "删除成功"))
            }
        }
    }
}
