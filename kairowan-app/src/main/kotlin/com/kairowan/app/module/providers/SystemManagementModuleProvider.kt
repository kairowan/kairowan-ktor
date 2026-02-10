package com.kairowan.app.module.providers

import com.kairowan.app.module.AppModuleProvider
import com.kairowan.core.controller.AuthenticatedRouteController
import com.kairowan.system.controller.FileController
import com.kairowan.system.controller.NotificationController
import com.kairowan.system.controller.ProfileController
import com.kairowan.system.controller.SysConfigController
import com.kairowan.system.controller.SysDeptController
import com.kairowan.system.controller.SysDictController
import com.kairowan.system.controller.SysMenuController
import com.kairowan.system.controller.SysPostController
import com.kairowan.system.controller.SysRoleController
import com.kairowan.system.controller.SysUserController
import com.kairowan.system.controller.ToolFileController
import com.kairowan.system.service.FileService
import com.kairowan.system.service.FileSyncService
import com.kairowan.system.service.NotificationService
import com.kairowan.system.service.ProfileService
import com.kairowan.system.service.SysConfigService
import com.kairowan.system.service.SysDeptService
import com.kairowan.system.service.SysDictService
import com.kairowan.system.service.SysMenuService
import com.kairowan.system.service.SysPermissionService
import com.kairowan.system.service.SysPostService
import com.kairowan.system.service.SysRoleService
import com.kairowan.system.service.SysUserService
import io.ktor.server.config.ApplicationConfig
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * 系统管理模块提供者。
 *
 * 聚合用户、角色、菜单、字典、配置、部门、文件等业务服务与控制器。
 */
class SystemManagementModuleProvider : AppModuleProvider {
    override fun provide(config: ApplicationConfig): Module = module {
        single { SysPermissionService(get(), get()) }
        single { SysUserService(get(), get()) }
        single { SysMenuService(get(), get()) }
        single { SysRoleService(get(), get()) }
        single { SysDictService(get(), get()) }
        single { SysConfigService(get(), get()) }
        single { SysDeptService(get()) }
        single { SysPostService(get()) }
        single { ProfileService(get()) }
        single { NotificationService(get()) }
        single { FileService(get()) }
        single { FileSyncService(get()) }

        single { ProfileController() } bind AuthenticatedRouteController::class
        single { NotificationController() } bind AuthenticatedRouteController::class
        single { FileController() } bind AuthenticatedRouteController::class
        single { ToolFileController() } bind AuthenticatedRouteController::class
        single { SysUserController() } bind AuthenticatedRouteController::class
        single { SysRoleController() } bind AuthenticatedRouteController::class
        single { SysMenuController() } bind AuthenticatedRouteController::class
        single { SysDeptController() } bind AuthenticatedRouteController::class
        single { SysPostController() } bind AuthenticatedRouteController::class
        single { SysConfigController() } bind AuthenticatedRouteController::class
        single { SysDictController() } bind AuthenticatedRouteController::class
    }
}
