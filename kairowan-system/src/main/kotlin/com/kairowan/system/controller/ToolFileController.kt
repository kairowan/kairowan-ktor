package com.kairowan.system.controller

import com.kairowan.common.KResult
import com.kairowan.common.utils.FileUploadUtils
import com.kairowan.core.controller.KController
import com.kairowan.core.framework.security.LoginUser
import com.kairowan.core.req.IdsReq
import com.kairowan.system.api.SystemApiRoutes
import com.kairowan.system.service.FileService
import com.kairowan.system.service.FileSyncService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.kairowan.core.controller.AuthenticatedRouteController

/**
 * 工具-文件上传控制器
 * @author Kairowan
 * @date 2026-01-30
 */
class ToolFileController : KController(), AuthenticatedRouteController {

    override fun register(route: Route) {
        route.routes()
    }

    private fun Route.routes() {
        val fileService by inject<FileService>()
        val fileSyncService by inject<FileSyncService>()

        route(SystemApiRoutes.ToolFile.ROOT) {
            // 同步物理文件到数据库
            post(SystemApiRoutes.ToolFile.SYNC) {
                val loginUser = call.principal<LoginUser>()
                    ?: return@post call.respond(KResult.fail<Any>("未登录"))

                // 是否清空数据库（默认 true）
                val clearBefore = call.request.queryParameters["clearBefore"]?.toBoolean() ?: true

                // 配置上传路径
                val uploadPath = call.application.environment.config.propertyOrNull("file.uploadPath")?.getString()
                    ?: "uploads"
                val syncMarker = call.application.environment.config.propertyOrNull("file.syncMarker")?.getString()
                    ?: ".kairowan_file_sync.done"

                // 配置文件访问URL前缀
                val fileUrlPrefix = call.application.environment.config.propertyOrNull("file.urlPrefix")?.getString()
                    ?: "http://localhost:8080/files"

                val result = fileSyncService.syncFilesFromDisk(
                    uploadPath = uploadPath,
                    fileUrlPrefix = fileUrlPrefix,
                    defaultUserId = loginUser.userId,
                    defaultUserName = loginUser.username,
                    clearBefore = clearBefore,
                    excludeFileNames = setOf(syncMarker)
                )

                call.respond(KResult.ok("同步完成", result))
            }

            // 上传文件
            post(SystemApiRoutes.ToolFile.UPLOAD) {
                val loginUser = call.principal<LoginUser>()
                    ?: return@post call.respond(KResult.fail<Any>("未登录"))

                val multipart = call.receiveMultipart()
                val uploadedFiles = mutableListOf<Map<String, Any?>>()
                var errorMessage: String? = null

                // 配置上传路径
                val uploadPath = call.application.environment.config.propertyOrNull("file.uploadPath")?.getString()
                    ?: "uploads"

                // 配置文件访问URL前缀
                val fileUrlPrefix = call.application.environment.config.propertyOrNull("file.urlPrefix")?.getString()
                    ?: "http://localhost:8080/files"

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            if (errorMessage == null) {
                                val fileName = part.originalFileName ?: "unknown"
                                val fileBytes = part.streamProvider().readBytes()
                                val fileSize = fileBytes.size.toLong()

                                // 验证文件大小（50MB）
                                val maxSize = 50 * 1024 * 1024L
                                if (!FileUploadUtils.validateFileSize(fileSize, maxSize)) {
                                    errorMessage = "文件大小超过限制（50MB）"
                                } else {
                                    // 保存文件
                                    val relativePath = FileUploadUtils.saveFile(
                                        fileBytes.inputStream(),
                                        fileName,
                                        uploadPath
                                    )

                                    val fileType = FileUploadUtils.getFileType(fileName)
                                    val fileUrl = "$fileUrlPrefix/$relativePath"

                                    // 保存到数据库
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

            get(SystemApiRoutes.ToolFile.LIST) {
                val type = call.request.queryParameters["type"]
                val keyword = call.request.queryParameters["keyword"]
                val page = getPageRequest(call)

                val list = fileService.getFileList(type, keyword, page)
                call.respond(KResult.ok(mapOf("total" to list.total, "rows" to list.rows)))
            }

            // 下载文件
            get(SystemApiRoutes.ToolFile.DOWNLOAD) {
                val fileId = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, KResult.fail<Any>("参数错误"))

                val file = fileService.getFileById(fileId)
                    ?: return@get call.respond(HttpStatusCode.NotFound, KResult.fail<Any>("文件不存在"))

                val uploadPath = call.application.environment.config.propertyOrNull("file.uploadPath")?.getString()
                    ?: "uploads"

                // 从URL中提取相对路径
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
            delete(SystemApiRoutes.ToolFile.DELETE_ONE) {
                val fileId = call.parameters["id"]?.toLongOrNull()
                    ?: return@delete call.respond(KResult.fail<Any>("参数错误"))

                val file = fileService.getFileById(fileId)
                if (file != null) {
                    // 删除物理文件
                    val uploadPath = call.application.environment.config.propertyOrNull("file.uploadPath")?.getString()
                        ?: "uploads"
                    val fileUrlPrefix = call.application.environment.config.propertyOrNull("file.urlPrefix")?.getString()
                        ?: "http://localhost:8080/files"
                    val relativePath = file.fileUrl.removePrefix("$fileUrlPrefix/")
                    FileUploadUtils.deleteFile(relativePath, uploadPath)
                }

                // 删除数据库记录
                fileService.deleteFile(fileId)
                call.respond(KResult.ok<Any>(msg = "删除成功"))
            }

            // 批量删除文件
            delete(SystemApiRoutes.ToolFile.DELETE_BATCH) {
                val params = call.receive<IdsReq>()
                val ids = params.ids
                if (ids.isEmpty()) {
                    return@delete call.respond(KResult.fail<Any>("参数错误"))
                }

                // 删除物理文件
                val uploadPath = call.application.environment.config.propertyOrNull("file.uploadPath")?.getString()
                    ?: "uploads"
                val fileUrlPrefix = call.application.environment.config.propertyOrNull("file.urlPrefix")?.getString()
                    ?: "http://localhost:8080/files"

                ids.forEach { fileId ->
                    val file = fileService.getFileById(fileId)
                    if (file != null) {
                        val relativePath = file.fileUrl.removePrefix("$fileUrlPrefix/")
                        FileUploadUtils.deleteFile(relativePath, uploadPath)
                    }
                }

                // 删除数据库记录
                fileService.batchDeleteFiles(ids)
                call.respond(KResult.ok<Any>(msg = "删除成功"))
            }
        }
    }
}
