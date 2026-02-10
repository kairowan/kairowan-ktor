package com.kairowan.app.module.providers

import com.kairowan.app.module.AppModuleProvider
import com.kairowan.core.controller.AuthenticatedRouteController
import com.kairowan.core.controller.PublicRouteController
import com.kairowan.system.controller.AuthController
import com.kairowan.system.controller.AuthenticatedController
import com.kairowan.system.controller.CaptchaController
import com.kairowan.system.service.CaptchaService
import com.kairowan.system.service.SysLoginService
import com.kairowan.system.service.TokenService
import io.ktor.server.config.ApplicationConfig
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * 认证模块提供者。
 *
 * 注册登录、验证码、令牌服务及认证相关控制器。
 */
class AuthenticationModuleProvider : AppModuleProvider {
    override fun provide(config: ApplicationConfig): Module = module {
        single { TokenService(get()) }
        single { CaptchaService(get()) }
        single { SysLoginService(get(), get(), get(), get()) }

        single { AuthController() } bind PublicRouteController::class
        single { CaptchaController() } bind PublicRouteController::class
        single { AuthenticatedController() } bind AuthenticatedRouteController::class
    }
}
