package com.kairowan.app.lifecycle.stages

import com.kairowan.app.lifecycle.ApplicationLifecycleStage
import com.kairowan.common.KResult
import com.kairowan.common.constant.ResultCode
import com.kairowan.common.exception.ServiceException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.respond
import org.slf4j.Logger

/**
 * 启动阶段：统一异常处理配置。
 *
 * 将验证异常、业务异常与系统异常映射为统一响应模型。
 */
class ExceptionHandlingStage : ApplicationLifecycleStage {
    override fun execute(application: Application, logger: Logger) {
        application.install(StatusPages) {
            exception<RequestValidationException> { call, cause ->
                call.respond(KResult.fail<Any>(ResultCode.BAD_REQUEST.code, cause.reasons.joinToString()))
            }

            exception<ServiceException> { call, cause ->
                call.respond(KResult.fail<Any>(cause.code, cause.message))
            }

            status(HttpStatusCode.Unauthorized) { call, _ ->
                call.respond(HttpStatusCode.OK, KResult.fail<Any>(ResultCode.UNAUTHORIZED))
            }

            status(HttpStatusCode.Forbidden) { call, _ ->
                call.respond(HttpStatusCode.OK, KResult.fail<Any>(ResultCode.FORBIDDEN))
            }

            exception<Throwable> { call, cause ->
                logger.error("Global Exception", cause)
                val msg = if (call.application.environment.developmentMode) {
                    "System Error: ${cause.localizedMessage}"
                } else {
                    "System Error"
                }
                call.respond(HttpStatusCode.InternalServerError, KResult.fail<Any>(msg))
            }
        }
    }
}
