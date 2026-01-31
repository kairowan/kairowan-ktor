package com.kairowan.system.controller

import com.kairowan.common.KResult
import com.kairowan.common.utils.FileUploadUtils
import com.kairowan.core.controller.KController
import com.kairowan.core.framework.security.LoginUser
import com.kairowan.system.api.SystemApiRoutes
import com.kairowan.system.service.ProfileService
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import com.kairowan.core.controller.AuthenticatedRouteController

/**
 * 个人中心控制器
 * @author Kairowan
 * @date 2026-01-29
 */
class ProfileController : KController(), AuthenticatedRouteController {

    override fun register(route: Route) {
        route.routes()
    }

    private fun Route.routes() {
        val profileService by inject<ProfileService>()

        route(SystemApiRoutes.Profile.ROOT) {
            get(SystemApiRoutes.Profile.INFO) {
                val loginUser = call.principal<LoginUser>()
                    ?: return@get call.respond(KResult.fail<Any>("未登录"))

                val info = profileService.getProfileInfo(loginUser.userId)
                call.respond(KResult.ok(info))
            }

            // 更新用户信息
            put(SystemApiRoutes.Profile.UPDATE) {
                val loginUser = call.principal<LoginUser>()
                    ?: return@put call.respond(KResult.fail<Any>("未登录"))

                val updates = call.receive<Map<String, Any?>>()
                profileService.updateProfile(loginUser.userId, updates)
                call.respond(KResult.ok<Any>(msg = "更新成功"))
            }

            // 修改密码
            put(SystemApiRoutes.Profile.PASSWORD) {
                val loginUser = call.principal<LoginUser>()
                    ?: return@put call.respond(KResult.fail<Any>("未登录"))

                val params = call.receive<Map<String, String>>()
                val oldPassword = params["oldPassword"] ?: return@put call.respond(KResult.fail<Any>("旧密码不能为空"))
                val newPassword = params["newPassword"] ?: return@put call.respond(KResult.fail<Any>("新密码不能为空"))

                profileService.updatePassword(loginUser.userId, oldPassword, newPassword)
                call.respond(KResult.ok<Any>(msg = "密码修改成功"))
            }

            // 上传头像
            post(SystemApiRoutes.Profile.AVATAR) {
                val loginUser = call.principal<LoginUser>()
                    ?: return@post call.respond(KResult.fail<Any>("未登录"))

                val multipart = call.receiveMultipart()
                var avatarUrl: String? = null
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
                                val fileName = part.originalFileName ?: "avatar.jpg"
                                val fileBytes = part.streamProvider().readBytes()
                                val fileSize = fileBytes.size.toLong()

                                // 验证文件大小（2MB）
                                val maxSize = 2 * 1024 * 1024L
                                if (!FileUploadUtils.validateFileSize(fileSize, maxSize)) {
                                    errorMessage = "图片大小超过限制（2MB）"
                                } else {
                                    // 验证文件类型
                                    val allowedExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
                                    if (!FileUploadUtils.validateFileType(fileName, allowedExtensions)) {
                                        errorMessage = "只支持图片格式"
                                    } else {
                                        // 保存文件到 avatars 子目录
                                        val relativePath = FileUploadUtils.saveFile(
                                            fileBytes.inputStream(),
                                            fileName,
                                            "$uploadPath/avatars"
                                        )

                                        avatarUrl = "$fileUrlPrefix/avatars/$relativePath"
                                    }
                                }
                            }
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                when {
                    errorMessage != null -> call.respond(KResult.fail<Any>(errorMessage!!))
                    avatarUrl == null -> call.respond(KResult.fail<Any>("没有文件被上传"))
                    else -> {
                        // 更新用户头像
                        profileService.updateAvatar(loginUser.userId, avatarUrl!!)
                        call.respond(KResult.ok("上传成功", mapOf("url" to avatarUrl)))
                    }
                }
            }

            get(SystemApiRoutes.Profile.LOGS) {
                val loginUser = call.principal<LoginUser>()
                    ?: return@get call.respond(KResult.fail<Any>("未登录"))

                val page = getPageRequest(call)
                val logs = profileService.getOperationLogs(loginUser.userId, page)
                call.respond(KResult.ok(mapOf("total" to logs.total, "rows" to logs.rows)))
            }
        }
    }
}
