package com.kairowan.system.domain

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import org.ktorm.schema.datetime
import org.ktorm.entity.Entity
import java.time.LocalDateTime

/**
 * 部门实体 (树形结构)
 * @author Kairowan
 * @date 2026-01-18
 */
interface SysDept : Entity<SysDept> {
    companion object : Entity.Factory<SysDept>()
    
    var deptId: Long
    var parentId: Long           // 父部门ID
    var ancestors: String        // 祖级列表 (如: 0,1,2)
    var deptName: String         // 部门名称
    var orderNum: Int            // 显示顺序
    var leader: String           // 负责人
    var phone: String            // 联系电话
    var email: String            // 邮箱
    var status: String           // 部门状态 (0正常 1停用)
    var createTime: LocalDateTime?
}

object SysDepts : Table<SysDept>("sys_dept") {
    val deptId = long("dept_id").primaryKey().bindTo { it.deptId }
    val parentId = long("parent_id").bindTo { it.parentId }
    val ancestors = varchar("ancestors").bindTo { it.ancestors }
    val deptName = varchar("dept_name").bindTo { it.deptName }
    val orderNum = int("order_num").bindTo { it.orderNum }
    val leader = varchar("leader").bindTo { it.leader }
    val phone = varchar("phone").bindTo { it.phone }
    val email = varchar("email").bindTo { it.email }
    val status = varchar("status").bindTo { it.status }
    val createTime = datetime("create_time").bindTo { it.createTime }
}

/**
 * 岗位实体
 * @author Kairowan
 * @date 2026-01-18
 */
interface SysPost : Entity<SysPost> {
    companion object : Entity.Factory<SysPost>()
    
    var postId: Long
    var postCode: String         // 岗位编码
    var postName: String         // 岗位名称
    var postSort: Int            // 显示顺序
    var status: String           // 状态 (0正常 1停用)
    var remark: String           // 备注
    var createTime: LocalDateTime?
}

object SysPosts : Table<SysPost>("sys_post") {
    val postId = long("post_id").primaryKey().bindTo { it.postId }
    val postCode = varchar("post_code").bindTo { it.postCode }
    val postName = varchar("post_name").bindTo { it.postName }
    val postSort = int("post_sort").bindTo { it.postSort }
    val status = varchar("status").bindTo { it.status }
    val remark = varchar("remark").bindTo { it.remark }
    val createTime = datetime("create_time").bindTo { it.createTime }
}
