package com.kairowan.system.controller

import com.kairowan.common.KResult
import com.kairowan.core.controller.KController
import com.kairowan.core.framework.security.LoginUser
import com.kairowan.core.req.IdsReq
import com.kairowan.system.api.SystemApiRoutes
import com.kairowan.system.service.NotificationService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import com.kairowan.core.controller.AuthenticatedRouteController

/**
 * 通知中心控制器
 * @author Kairowan
 * @date 2026-01-29
 */
class NotificationController : KController(), AuthenticatedRouteController {

    override fun register(route: Route) {
        route.routes()
    }

    private fun Route.routes() {
        val notificationService by inject<NotificationService>()

        route(SystemApiRoutes.Notification.ROOT) {
            get(SystemApiRoutes.Notification.LIST) {
                val loginUser = call.principal<LoginUser>()
                    ?: return@get call.respond(KResult.fail<Any>("未登录"))

                val type = call.request.queryParameters["type"]
                val unread = call.request.queryParameters["unread"]?.toBooleanStrictOrNull()
                val read = when {
                    unread == null -> call.request.queryParameters["read"]?.toBooleanStrictOrNull()
                    else -> !unread
                }
                val page = getPageRequest(call)

                val list = notificationService.getNotificationList(loginUser.userId, type, read, page)
                call.respond(KResult.ok(mapOf("total" to list.total, "rows" to list.rows)))
            }

            get(SystemApiRoutes.Notification.STATS) {
                val loginUser = call.principal<LoginUser>()
                    ?: return@get call.respond(KResult.fail<Any>("未登录"))

                val stats = notificationService.getStats(loginUser.userId)
                call.respond(KResult.ok(stats))
            }

            // 标记单条已读
            put(SystemApiRoutes.Notification.READ_ONE) {
                val loginUser = call.principal<LoginUser>()
                    ?: return@put call.respond(KResult.fail<Any>("未登录"))

                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@put call.respond(KResult.fail<Any>("参数错误"))

                notificationService.markAsRead(loginUser.userId, listOf(id))
                call.respond(KResult.ok<Any>(msg = "标记成功"))
            }

            // 批量标记已读
            put(SystemApiRoutes.Notification.READ) {
                val loginUser = call.principal<LoginUser>()
                    ?: return@put call.respond(KResult.fail<Any>("未登录"))

                val params = call.receive<IdsReq>()
                val ids = params.ids
                if (ids.isEmpty()) {
                    return@put call.respond(KResult.fail<Any>("参数错误"))
                }

                notificationService.markAsRead(loginUser.userId, ids)
                call.respond(KResult.ok<Any>(msg = "标记成功"))
            }

            // 全部标记已读
            put(SystemApiRoutes.Notification.READ_ALL) {
                val loginUser = call.principal<LoginUser>()
                    ?: return@put call.respond(KResult.fail<Any>("未登录"))

                notificationService.markAllAsRead(loginUser.userId)
                call.respond(KResult.ok<Any>(msg = "标记成功"))
            }

            // 删除单条
            delete(SystemApiRoutes.Notification.DELETE_ONE) {
                val loginUser = call.principal<LoginUser>()
                    ?: return@delete call.respond(KResult.fail<Any>("未登录"))

                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@delete call.respond(KResult.fail<Any>("参数错误"))

                notificationService.deleteNotifications(loginUser.userId, listOf(id))
                call.respond(KResult.ok<Any>(msg = "删除成功"))
            }

            // 批量删除
            delete {
                val loginUser = call.principal<LoginUser>()
                    ?: return@delete call.respond(KResult.fail<Any>("未登录"))

                val params = call.receive<IdsReq>()
                val ids = params.ids
                if (ids.isEmpty()) {
                    return@delete call.respond(KResult.fail<Any>("参数错误"))
                }

                notificationService.deleteNotifications(loginUser.userId, ids)
                call.respond(KResult.ok<Any>(msg = "删除成功"))
            }

            get(SystemApiRoutes.Notification.UNREAD_COUNT) {
                val loginUser = call.principal<LoginUser>()
                    ?: return@get call.respond(KResult.fail<Any>("未登录"))

                val count = notificationService.getUnreadCount(loginUser.userId)
                call.respond(KResult.ok(count))
            }
        }
    }
}
