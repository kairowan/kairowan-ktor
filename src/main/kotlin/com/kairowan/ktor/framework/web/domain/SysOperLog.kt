package com.kairowan.ktor.framework.web.domain

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import org.ktorm.schema.datetime
import org.ktorm.entity.Entity
import java.time.LocalDateTime

/**
 * 操作日志实体
 * @author Kairowan
 * @date 2026-01-18
 */
interface SysOperLog : Entity<SysOperLog> {
    companion object : Entity.Factory<SysOperLog>()
    
    var operId: Long
    var title: String            // 模块标题
    var businessType: Int        // 业务类型 (0其它 1新增 2修改 3删除)
    var method: String           // 方法名称
    var requestMethod: String    // 请求方式
    var operName: String         // 操作人员
    var operUrl: String          // 请求URL
    var operIp: String           // 主机地址
    var operParam: String        // 请求参数
    var jsonResult: String       // 返回参数
    var status: Int              // 操作状态 (0正常 1异常)
    var errorMsg: String         // 错误消息
    var operTime: LocalDateTime?
}

object SysOperLogs : Table<SysOperLog>("sys_oper_log") {
    val operId = long("oper_id").primaryKey().bindTo { it.operId }
    val title = varchar("title").bindTo { it.title }
    val businessType = int("business_type").bindTo { it.businessType }
    val method = varchar("method").bindTo { it.method }
    val requestMethod = varchar("request_method").bindTo { it.requestMethod }
    val operName = varchar("oper_name").bindTo { it.operName }
    val operUrl = varchar("oper_url").bindTo { it.operUrl }
    val operIp = varchar("oper_ip").bindTo { it.operIp }
    val operParam = varchar("oper_param").bindTo { it.operParam }
    val jsonResult = varchar("json_result").bindTo { it.jsonResult }
    val status = int("status").bindTo { it.status }
    val errorMsg = varchar("error_msg").bindTo { it.errorMsg }
    val operTime = datetime("oper_time").bindTo { it.operTime }
}
