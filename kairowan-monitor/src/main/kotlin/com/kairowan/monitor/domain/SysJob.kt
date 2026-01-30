package com.kairowan.ktor.framework.web.domain

import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import org.ktorm.schema.int
import org.ktorm.schema.datetime
import org.ktorm.entity.Entity
import java.time.LocalDateTime

/**
 * 定时任务实体
 * @author Kairowan
 * @date 2026-01-18
 */
interface SysJob : Entity<SysJob> {
    companion object : Entity.Factory<SysJob>()
    
    var jobId: Long
    var jobName: String          // 任务名称
    var jobGroup: String         // 任务组名
    var invokeTarget: String     // 调用目标 (类全名)
    var cronExpression: String   // CRON 表达式
    var misfirePolicy: String    // 计划执行错误策略 (1立即执行 2执行一次 3放弃执行)
    var concurrent: String       // 是否并发执行 (0允许 1禁止)
    var status: String           // 状态 (0正常 1暂停)
    var remark: String           // 备注
    var createTime: LocalDateTime?
}

object SysJobs : Table<SysJob>("sys_job") {
    val jobId = long("job_id").primaryKey().bindTo { it.jobId }
    val jobName = varchar("job_name").bindTo { it.jobName }
    val jobGroup = varchar("job_group").bindTo { it.jobGroup }
    val invokeTarget = varchar("invoke_target").bindTo { it.invokeTarget }
    val cronExpression = varchar("cron_expression").bindTo { it.cronExpression }
    val misfirePolicy = varchar("misfire_policy").bindTo { it.misfirePolicy }
    val concurrent = varchar("concurrent").bindTo { it.concurrent }
    val status = varchar("status").bindTo { it.status }
    val remark = varchar("remark").bindTo { it.remark }
    val createTime = datetime("create_time").bindTo { it.createTime }
}

/**
 * 任务执行日志实体
 */
interface SysJobLog : Entity<SysJobLog> {
    companion object : Entity.Factory<SysJobLog>()
    
    var jobLogId: Long
    var jobName: String
    var jobGroup: String
    var invokeTarget: String
    var jobMessage: String
    var status: String           // 执行状态 (0正常 1失败)
    var exceptionInfo: String    // 异常信息
    var createTime: LocalDateTime?
}

object SysJobLogs : Table<SysJobLog>("sys_job_log") {
    val jobLogId = long("job_log_id").primaryKey().bindTo { it.jobLogId }
    val jobName = varchar("job_name").bindTo { it.jobName }
    val jobGroup = varchar("job_group").bindTo { it.jobGroup }
    val invokeTarget = varchar("invoke_target").bindTo { it.invokeTarget }
    val jobMessage = varchar("job_message").bindTo { it.jobMessage }
    val status = varchar("status").bindTo { it.status }
    val exceptionInfo = varchar("exception_info").bindTo { it.exceptionInfo }
    val createTime = datetime("create_time").bindTo { it.createTime }
}
