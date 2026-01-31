package com.kairowan.monitor.controller

import com.kairowan.common.KResult
import com.kairowan.core.controller.AuthenticatedRouteController
import com.kairowan.core.controller.KController
import com.kairowan.core.framework.security.requirePermission
import com.kairowan.core.req.IdsReq
import com.kairowan.monitor.api.MonitorApiRoutes
import com.kairowan.monitor.domain.SysJob
import com.kairowan.monitor.service.OnlineUserService
import com.kairowan.monitor.service.ServerMonitorService
import com.kairowan.monitor.service.SysJobService
import com.kairowan.monitor.service.SysLogService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

/**
 * 监控模块路由
 * @author Kairowan
 * @date 2026-01-29
 */
class MonitorController : KController(), AuthenticatedRouteController {

    override fun register(route: Route) {
        route.routes()
    }

    private fun Route.routes() {
        val onlineUserService by inject<OnlineUserService>()
        val serverMonitorService by inject<ServerMonitorService>()
        val jobService by inject<SysJobService>()
        val logService by inject<SysLogService>()

        route(MonitorApiRoutes.Monitor.ROOT) {
            // ==================== 在线用户 ====================
            route(MonitorApiRoutes.Monitor.ONLINE) {
                requirePermission("monitor:online:list") {
                    get(MonitorApiRoutes.Monitor.ONLINE_LIST) {
                        val users = onlineUserService.getOnlineUsers()
                        call.respond(KResult.ok(mapOf("total" to users.size, "rows" to users)))
                    }
                }

                requirePermission("monitor:online:list") {
                    get(MonitorApiRoutes.Monitor.ONLINE_COUNT) {
                        val count = onlineUserService.getOnlineCount()
                        call.respond(KResult.ok(count))
                    }
                }

                // 强制踢出用户
                requirePermission("monitor:online:forceLogout") {
                    delete(MonitorApiRoutes.Monitor.ONLINE_FORCE) {
                        val tokenId = call.parameters["tokenId"] ?: throw IllegalArgumentException("tokenId is required")
                        val success = onlineUserService.forceLogout(tokenId)
                        call.respond(toAjax(success))
                    }
                }
            }

            // ==================== 服务监控 ====================
            requirePermission("monitor:server:list") {
                get(MonitorApiRoutes.Monitor.SERVER) {
                    val serverInfo = serverMonitorService.getServerInfo()
                    call.respond(KResult.ok(serverInfo))
                }
            }

            // ==================== 定时任务 ====================
            route(MonitorApiRoutes.Job.ROOT) {
                requirePermission("monitor:job:list") {
                    get(MonitorApiRoutes.Job.LIST) {
                        val page = getPageRequest(call)
                        val list = jobService.list(page)
                        call.respond(KResult.ok(mapOf("total" to list.total, "rows" to list.rows)))
                    }
                }

                requirePermission("monitor:job:query") {
                    get(MonitorApiRoutes.Job.DETAIL) {
                        val jobId = call.parameters["jobId"]?.toLongOrNull() ?: throw IllegalArgumentException("jobId is required")
                        val job = jobService.getById(jobId)
                        call.respond(KResult.ok(job))
                    }
                }

                requirePermission("monitor:job:list") {
                    get(MonitorApiRoutes.Job.RUNNING) {
                        val jobs = jobService.getRunningJobs()
                        call.respond(KResult.ok(jobs))
                    }
                }

                // 新增任务
                requirePermission("monitor:job:add") {
                    post {
                        val job = call.receive<SysJob>()
                        jobService.createJob(job)
                        call.respond(toAjax(true))
                    }
                }

                // 修改任务
                requirePermission("monitor:job:edit") {
                    put {
                        val job = call.receive<SysJob>()
                        jobService.updateJob(job)
                        call.respond(toAjax(true))
                    }
                }

                // 删除任务
                requirePermission("monitor:job:remove") {
                    delete(MonitorApiRoutes.Job.DETAIL) {
                        val jobId = call.parameters["jobId"]?.toLongOrNull() ?: throw IllegalArgumentException("jobId is required")
                        jobService.deleteJob(jobId)
                        call.respond(toAjax(true))
                    }
                }

                // 暂停任务
                requirePermission("monitor:job:changeStatus") {
                    put(MonitorApiRoutes.Job.PAUSE) {
                        val jobId = call.parameters["jobId"]?.toLongOrNull() ?: throw IllegalArgumentException("jobId is required")
                        jobService.pauseJob(jobId)
                        call.respond(toAjax(true))
                    }
                }

                // 恢复任务
                requirePermission("monitor:job:changeStatus") {
                    put(MonitorApiRoutes.Job.RESUME) {
                        val jobId = call.parameters["jobId"]?.toLongOrNull() ?: throw IllegalArgumentException("jobId is required")
                        jobService.resumeJob(jobId)
                        call.respond(toAjax(true))
                    }
                }

                // 立即执行一次
                requirePermission("monitor:job:changeStatus") {
                    put(MonitorApiRoutes.Job.RUN) {
                        val jobId = call.parameters["jobId"]?.toLongOrNull() ?: throw IllegalArgumentException("jobId is required")
                        jobService.runOnce(jobId)
                        call.respond(toAjax(true))
                    }
                }
            }

            // ==================== 操作日志 ====================
            route(MonitorApiRoutes.OperLog.ROOT) {
                requirePermission("monitor:operlog:list") {
                    get(MonitorApiRoutes.OperLog.LIST) {
                        val page = getPageRequest(call)
                        val list = logService.listOperLog(page)
                        call.respond(KResult.ok(mapOf("total" to list.total, "rows" to list.rows)))
                    }
                }

                requirePermission("monitor:operlog:query") {
                    get(MonitorApiRoutes.OperLog.DETAIL) {
                        val operId = call.parameters["operId"]?.toLongOrNull() ?: throw IllegalArgumentException("operId is required")
                        val detail = logService.getOperLogById(operId)
                            ?: return@get call.respond(KResult.fail<Any>("记录不存在"))
                        call.respond(KResult.ok(detail))
                    }
                }

                // 删除操作日志
                requirePermission("monitor:operlog:remove") {
                    delete {
                        val params = call.receive<IdsReq>()
                        val ids = params.ids
                        if (ids.isEmpty()) {
                            return@delete call.respond(KResult.fail<Any>("参数错误"))
                        }
                        logService.deleteOperLog(ids)
                        call.respond(toAjax(true))
                    }
                }

                // 清空操作日志
                requirePermission("monitor:operlog:remove") {
                    delete(MonitorApiRoutes.OperLog.CLEAN) {
                        logService.cleanOperLog()
                        call.respond(toAjax(true))
                    }
                }
            }

            // ==================== 登录日志 ====================
            route(MonitorApiRoutes.LoginInfo.ROOT) {
                requirePermission("monitor:logininfor:list") {
                    get(MonitorApiRoutes.LoginInfo.LIST) {
                        val page = getPageRequest(call)
                        val list = logService.listLoginLog(page)
                        call.respond(KResult.ok(mapOf("total" to list.total, "rows" to list.rows)))
                    }
                }

                // 删除登录日志
                requirePermission("monitor:logininfor:remove") {
                    delete {
                        val params = call.receive<IdsReq>()
                        val ids = params.ids
                        if (ids.isEmpty()) {
                            return@delete call.respond(KResult.fail<Any>("参数错误"))
                        }
                        logService.deleteLoginLog(ids)
                        call.respond(toAjax(true))
                    }
                }

                // 清空登录日志
                requirePermission("monitor:logininfor:remove") {
                    delete(MonitorApiRoutes.LoginInfo.CLEAN) {
                        logService.cleanLoginLog()
                        call.respond(toAjax(true))
                    }
                }
            }
        }
    }
}
