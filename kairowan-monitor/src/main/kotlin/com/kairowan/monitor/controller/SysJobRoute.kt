package com.kairowan.ktor.framework.web.controller

import com.kairowan.ktor.common.KResult
import com.kairowan.ktor.framework.security.requirePermission
import com.kairowan.ktor.framework.web.domain.SysJob
import com.kairowan.ktor.framework.web.service.SysJobService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import io.github.smiley4.ktorswaggerui.dsl.*

/**
 * 定时任务控制器
 * @author Kairowan
 * @date 2026-01-18
 */
class SysJobController : KController() {

    fun Route.routes() {
        val jobService by inject<SysJobService>()

        route("/monitor/job") {
            // 任务列表
            get("/list", {
                tags = listOf("Job Management")
                summary = "获取定时任务列表"
                securitySchemeName = "BearerAuth"
            }) {
                val page = getPageRequest(call)
                val list = jobService.list(page)
                call.respond(list)
            }

            // 任务详情
            get("/{jobId}", {
                tags = listOf("Job Management")
                summary = "获取任务详情"
                securitySchemeName = "BearerAuth"
            }) {
                val jobId = call.parameters["jobId"]?.toLongOrNull()
                    ?: return@get call.respond(KResult.fail<Any>("参数错误"))
                val job = jobService.getById(jobId)
                call.respond(KResult.ok(job))
            }

            // 调度器中的运行状态
            get("/running", {
                tags = listOf("Job Management")
                summary = "获取调度器中运行的任务状态"
                securitySchemeName = "BearerAuth"
            }) {
                val jobs = jobService.getRunningJobs()
                call.respond(KResult.ok(jobs))
            }

            // 新增任务
            requirePermission("monitor:job:add") {
                post({
                    tags = listOf("Job Management")
                    summary = "新增定时任务"
                    securitySchemeName = "BearerAuth"
                }) {
                    val job = call.receive<SysJob>()
                    jobService.createJob(job)
                    call.respond(toAjax(true))
                }
            }

            // 修改任务
            requirePermission("monitor:job:edit") {
                put({
                    tags = listOf("Job Management")
                    summary = "修改定时任务"
                    securitySchemeName = "BearerAuth"
                }) {
                    val job = call.receive<SysJob>()
                    jobService.updateJob(job)
                    call.respond(toAjax(true))
                }
            }

            // 删除任务
            requirePermission("monitor:job:remove") {
                delete("/{jobId}", {
                    tags = listOf("Job Management")
                    summary = "删除定时任务"
                    securitySchemeName = "BearerAuth"
                }) {
                    val jobId = call.parameters["jobId"]?.toLongOrNull()
                        ?: return@delete call.respond(KResult.fail<Any>("参数错误"))
                    jobService.deleteJob(jobId)
                    call.respond(toAjax(true))
                }
            }

            // 暂停任务
            requirePermission("monitor:job:changeStatus") {
                put("/pause/{jobId}", {
                    tags = listOf("Job Management")
                    summary = "暂停定时任务"
                    securitySchemeName = "BearerAuth"
                }) {
                    val jobId = call.parameters["jobId"]?.toLongOrNull()
                        ?: return@put call.respond(KResult.fail<Any>("参数错误"))
                    jobService.pauseJob(jobId)
                    call.respond(KResult.ok<Any>(msg = "暂停成功"))
                }
            }

            // 恢复任务
            requirePermission("monitor:job:changeStatus") {
                put("/resume/{jobId}", {
                    tags = listOf("Job Management")
                    summary = "恢复定时任务"
                    securitySchemeName = "BearerAuth"
                }) {
                    val jobId = call.parameters["jobId"]?.toLongOrNull()
                        ?: return@put call.respond(KResult.fail<Any>("参数错误"))
                    jobService.resumeJob(jobId)
                    call.respond(KResult.ok<Any>(msg = "恢复成功"))
                }
            }

            // 立即执行一次
            requirePermission("monitor:job:changeStatus") {
                put("/run/{jobId}", {
                    tags = listOf("Job Management")
                    summary = "立即执行一次"
                    securitySchemeName = "BearerAuth"
                }) {
                    val jobId = call.parameters["jobId"]?.toLongOrNull()
                        ?: return@put call.respond(KResult.fail<Any>("参数错误"))
                    jobService.runOnce(jobId)
                    call.respond(KResult.ok<Any>(msg = "执行成功"))
                }
            }
        }
    }
}

fun Route.sysJobRoutes() {
    SysJobController().apply { routes() }
}
