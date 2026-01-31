package com.kairowan.system.domain

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import org.ktorm.schema.datetime
import org.ktorm.entity.Entity
import java.time.LocalDateTime

/**
 * 系统用户实体
 * @author Kairowan
 * @date 2026-01-18
 */
interface SysUser : Entity<SysUser> {
    companion object : Entity.Factory<SysUser>()

    var userId: Int
    var userName: String
    var nickName: String
    var password: String
    var status: String       // 0正常 1停用
    var email: String
    var phone: String
    var gender: String?      // 性别 0男 1女 2未知
    var avatar: String?      // 头像
    var remark: String?      // 备注
    var deptId: Int?
    var createTime: LocalDateTime?
}

object SysUsers : Table<SysUser>("sys_user") {
    val userId = int("user_id").primaryKey().bindTo { it.userId }
    val userName = varchar("user_name").bindTo { it.userName }
    val nickName = varchar("nick_name").bindTo { it.nickName }
    val password = varchar("password").bindTo { it.password }
    val status = varchar("status").bindTo { it.status }
    val email = varchar("email").bindTo { it.email }
    val phone = varchar("phone").bindTo { it.phone }
    val gender = varchar("gender").bindTo { it.gender }
    val avatar = varchar("avatar").bindTo { it.avatar }
    val remark = varchar("remark").bindTo { it.remark }
    val deptId = int("dept_id").bindTo { it.deptId }
    val createTime = datetime("create_time").bindTo { it.createTime }
}

