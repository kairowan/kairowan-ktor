package com.kairowan.app.lifecycle

import io.ktor.server.application.Application
import org.slf4j.Logger

/**
 * 应用启动生命周期阶段。
 *
 * 每个阶段负责一个清晰职责（例如插件安装、路由注册、预热等）。
 */
interface ApplicationLifecycleStage {
    fun execute(application: Application, logger: Logger)
}
