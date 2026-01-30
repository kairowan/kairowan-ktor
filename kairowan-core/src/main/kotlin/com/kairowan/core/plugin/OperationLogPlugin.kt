package com.kairowan.ktor.framework.web.plugin

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kairowan.ktor.common.utils.IpUtils
import com.kairowan.ktor.framework.security.getLoginUser
import com.kairowan.ktor.framework.web.domain.SysOperLog
import com.kairowan.ktor.framework.web.service.SysOperLogService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.koin.ktor.ext.inject
import java.time.LocalDateTime

/**
 * 操作日志记录插件
 * 自动记录标记了 @Log 注解的接口调用
 * 
 * @author Kairowan
 * @date 2026-01-18
 */
val OperationLogPlugin = createRouteScopedPlugin(
    name = "OperationLogPlugin",
    createConfiguration = ::OperationLogConfig
) {
    val config = pluginConfig
    val mapper = jacksonObjectMapper()

    onCall { call ->
        call.attributes.put(StartTimeKey, System.currentTimeMillis())
    }

    onCallRespond { call, _ ->
        // 只记录指定路径的操作日志
        val path = call.request.path()
        val method = call.request.httpMethod
        
        // 跳过不需要记录的路径
        if (shouldSkipLog(path, method)) {
            return@onCallRespond
        }

        try {
            val logService by call.application.inject<SysOperLogService>()
            val startTime = call.attributes.getOrNull(StartTimeKey)
            val costTime = if (startTime != null) System.currentTimeMillis() - startTime else 0

            val loginUser = call.getLoginUser()
            val operName = loginUser?.username ?: "anonymous"

            val operLog = SysOperLog {
                this.title = getModuleTitle(path)
                this.businessType = getBusinessType(method)
                this.method = "${method.value} $path"
                this.requestMethod = method.value
                this.operName = operName
                this.operUrl = path
                this.operIp = IpUtils.getClientIp(call.request)
                this.operParam = "" // 可以从 call 获取请求参数
                this.jsonResult = ""
                this.status = 0 // 成功
                this.errorMsg = ""
                this.operTime = LocalDateTime.now()
            }

            logService.recordOperLogAsync(operLog)
        } catch (e: Exception) {
            // 日志记录失败不影响主流程
        }
    }
}

private val StartTimeKey = AttributeKey<Long>("OperLogStartTime")

private fun shouldSkipLog(path: String, method: HttpMethod): Boolean {
    // GET 请求默认不记录 (除非特殊配置)
    if (method == HttpMethod.Get) {
        return true
    }
    // 跳过以下路径
    val skipPaths = listOf("/login", "/logout", "/metrics", "/swagger-ui", "/common/download")
    return skipPaths.any { path.startsWith(it) }
}

private fun getModuleTitle(path: String): String {
    return when {
        path.contains("/system/user") -> "用户管理"
        path.contains("/system/role") -> "角色管理"
        path.contains("/system/menu") -> "菜单管理"
        path.contains("/system/config") -> "配置管理"
        path.contains("/system/dict") -> "字典管理"
        path.contains("/system/dept") -> "部门管理"
        path.contains("/system/post") -> "岗位管理"
        path.contains("/common") -> "通用功能"
        else -> "其他"
    }
}

private fun getBusinessType(method: HttpMethod): Int {
    return when (method) {
        HttpMethod.Post -> 1   // 新增
        HttpMethod.Put -> 2    // 修改
        HttpMethod.Delete -> 3 // 删除
        HttpMethod.Get -> 4    // 查询
        else -> 0              // 其它
    }
}

class OperationLogConfig {
    var enabled: Boolean = true
    var skipPaths: List<String> = emptyList()
}
