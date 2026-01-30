package com.kairowan.ktor.framework.web.domain

import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import org.ktorm.schema.datetime
import org.ktorm.entity.Entity
import java.time.LocalDateTime

/**
 * 登录日志实体
 * @author Kairowan
 * @date 2026-01-18
 */
interface SysLoginLog : Entity<SysLoginLog> {
    companion object : Entity.Factory<SysLoginLog>()
    
    var infoId: Long
    var userName: String         // 用户账号
    var ipaddr: String           // 登录IP地址
    var loginLocation: String    // 登录地点
    var browser: String          // 浏览器类型
    var os: String               // 操作系统
    var status: String           // 登录状态 (0成功 1失败)
    var msg: String              // 提示消息
    var loginTime: LocalDateTime?
}

object SysLoginLogs : Table<SysLoginLog>("sys_login_log") {
    val infoId = long("info_id").primaryKey().bindTo { it.infoId }
    val userName = varchar("user_name").bindTo { it.userName }
    val ipaddr = varchar("ipaddr").bindTo { it.ipaddr }
    val loginLocation = varchar("login_location").bindTo { it.loginLocation }
    val browser = varchar("browser").bindTo { it.browser }
    val os = varchar("os").bindTo { it.os }
    val status = varchar("status").bindTo { it.status }
    val msg = varchar("msg").bindTo { it.msg }
    val loginTime = datetime("login_time").bindTo { it.loginTime }
}
