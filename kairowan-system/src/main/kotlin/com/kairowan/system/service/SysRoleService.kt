package com.kairowan.ktor.framework.web.service

import com.kairowan.ktor.framework.web.domain.SysRole
import com.kairowan.ktor.framework.web.domain.SysRoles
import org.ktorm.database.Database

/**
 * 角色服务
 * @author Kairowan
 * @date 2026-01-18
 */
class SysRoleService(database: Database) : KService<SysRole>(database, SysRoles)
