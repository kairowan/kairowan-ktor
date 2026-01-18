package com.kairowan.ktor.framework.web.service

import com.kairowan.ktor.framework.manager.AsyncManager
import com.kairowan.ktor.framework.web.domain.*
import com.kairowan.ktor.framework.web.page.KPageRequest
import com.kairowan.ktor.framework.web.page.KTableData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import org.ktorm.entity.update
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import org.quartz.impl.matchers.GroupMatcher
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

/**
 * 定时任务服务
 * @author Kairowan
 * @date 2026-01-18
 */
class SysJobService(private val database: Database) {

    private val logger = LoggerFactory.getLogger(SysJobService::class.java)
    private val scheduler: Scheduler = StdSchedulerFactory.getDefaultScheduler()

    companion object {
        const val JOB_GROUP = "DEFAULT"
    }

    /**
     * 获取任务列表
     */
    suspend fun list(page: KPageRequest? = null): KTableData = withContext(Dispatchers.IO) {
        val query = database.from(SysJobs)
            .select()
            .orderBy(SysJobs.jobId.asc())

        if (page != null) {
            val offset = page.getOffset()
            query.limit(offset, page.pageSize)
            val total = database.from(SysJobs).select(count()).map { it.getInt(1) }.first().toLong()
            val list = query.map { SysJobs.createEntity(it) }
            KTableData.build(list, total)
        } else {
            val list = query.map { SysJobs.createEntity(it) }
            KTableData.build(list)
        }
    }

    /**
     * 获取任务详情
     */
    suspend fun getById(jobId: Long): SysJob? = withContext(Dispatchers.IO) {
        database.sequenceOf(SysJobs).find { it.jobId eq jobId }
    }

    /**
     * 创建任务
     */
    suspend fun createJob(job: SysJob): Boolean = withContext(Dispatchers.IO) {
        // 验证 CRON 表达式
        if (!CronExpression.isValidExpression(job.cronExpression)) {
            throw IllegalArgumentException("无效的CRON表达式: ${job.cronExpression}")
        }
        
        database.sequenceOf(SysJobs).add(job)
        
        // 如果状态正常，添加到调度器
        if (job.status == "0") {
            addToScheduler(job)
        }
        true
    }

    /**
     * 更新任务
     */
    suspend fun updateJob(job: SysJob): Boolean = withContext(Dispatchers.IO) {
        if (!CronExpression.isValidExpression(job.cronExpression)) {
            throw IllegalArgumentException("无效的CRON表达式: ${job.cronExpression}")
        }
        
        database.sequenceOf(SysJobs).update(job)
        
        // 重新调度
        val jobKey = JobKey.jobKey(job.jobName, job.jobGroup)
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey)
        }
        
        if (job.status == "0") {
            addToScheduler(job)
        }
        true
    }

    /**
     * 删除任务
     */
    suspend fun deleteJob(jobId: Long): Boolean = withContext(Dispatchers.IO) {
        val job = database.sequenceOf(SysJobs).find { it.jobId eq jobId } ?: return@withContext false
        
        // 从调度器删除
        val jobKey = JobKey.jobKey(job.jobName, job.jobGroup)
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey)
        }
        
        database.delete(SysJobs) { it.jobId eq jobId }
        true
    }

    /**
     * 暂停任务
     */
    suspend fun pauseJob(jobId: Long): Boolean = withContext(Dispatchers.IO) {
        val job = database.sequenceOf(SysJobs).find { it.jobId eq jobId } ?: return@withContext false
        
        val jobKey = JobKey.jobKey(job.jobName, job.jobGroup)
        if (scheduler.checkExists(jobKey)) {
            scheduler.pauseJob(jobKey)
        }
        
        database.update(SysJobs) {
            set(it.status, "1")
            where { it.jobId eq jobId }
        }
        true
    }

    /**
     * 恢复任务
     */
    suspend fun resumeJob(jobId: Long): Boolean = withContext(Dispatchers.IO) {
        val job = database.sequenceOf(SysJobs).find { it.jobId eq jobId } ?: return@withContext false
        
        val jobKey = JobKey.jobKey(job.jobName, job.jobGroup)
        
        if (!scheduler.checkExists(jobKey)) {
            addToScheduler(job)
        } else {
            scheduler.resumeJob(jobKey)
        }
        
        database.update(SysJobs) {
            set(it.status, "0")
            where { it.jobId eq jobId }
        }
        true
    }

    /**
     * 立即执行一次
     */
    suspend fun runOnce(jobId: Long): Boolean = withContext(Dispatchers.IO) {
        val job = database.sequenceOf(SysJobs).find { it.jobId eq jobId } ?: return@withContext false
        
        val jobKey = JobKey.jobKey(job.jobName, job.jobGroup)
        
        if (scheduler.checkExists(jobKey)) {
            scheduler.triggerJob(jobKey)
        } else {
            // 临时添加执行
            addToScheduler(job)
            scheduler.triggerJob(jobKey)
        }
        true
    }

    /**
     * 获取调度器中的所有任务状态
     */
    fun getRunningJobs(): List<JobStatusVo> {
        val result = mutableListOf<JobStatusVo>()
        
        val groupNames = scheduler.jobGroupNames
        for (groupName in groupNames) {
            val jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))
            for (jobKey in jobKeys) {
                val triggers = scheduler.getTriggersOfJob(jobKey)
                val trigger = triggers.firstOrNull()
                
                result.add(JobStatusVo(
                    jobName = jobKey.name,
                    jobGroup = jobKey.group,
                    triggerState = trigger?.let { scheduler.getTriggerState(it.key).name } ?: "NONE",
                    nextFireTime = trigger?.nextFireTime?.toString() ?: "",
                    previousFireTime = trigger?.previousFireTime?.toString() ?: ""
                ))
            }
        }
        
        return result
    }

    /**
     * 添加任务到调度器
     */
    private fun addToScheduler(job: SysJob) {
        try {
            val jobDetail = JobBuilder.newJob(GenericJob::class.java)
                .withIdentity(job.jobName, job.jobGroup)
                .usingJobData("invokeTarget", job.invokeTarget)
                .usingJobData("jobId", job.jobId)
                .build()

            val trigger = TriggerBuilder.newTrigger()
                .withIdentity("${job.jobName}-Trigger", job.jobGroup)
                .withSchedule(CronScheduleBuilder.cronSchedule(job.cronExpression))
                .build()

            scheduler.scheduleJob(jobDetail, trigger)
            logger.info("Added job to scheduler: ${job.jobName}")
        } catch (e: Exception) {
            logger.error("Failed to add job: ${job.jobName}", e)
        }
    }
}

/**
 * 任务状态 VO
 */
data class JobStatusVo(
    val jobName: String,
    val jobGroup: String,
    val triggerState: String,
    val nextFireTime: String,
    val previousFireTime: String
)

/**
 * 通用任务执行器
 * 根据 invokeTarget 动态调用目标方法
 */
class GenericJob : Job {
    private val logger = LoggerFactory.getLogger(GenericJob::class.java)
    
    override fun execute(context: JobExecutionContext) {
        val dataMap = context.jobDetail.jobDataMap
        val invokeTarget = dataMap.getString("invokeTarget")
        val jobId = dataMap.getLong("jobId")
        
        logger.info("Executing job: $invokeTarget (ID: $jobId)")
        
        try {
            // 反射调用目标类的 execute 方法
            val clazz = Class.forName(invokeTarget)
            val instance = clazz.getDeclaredConstructor().newInstance()
            val method = clazz.getMethod("execute")
            method.invoke(instance)
            
            logger.info("Job completed successfully: $invokeTarget")
        } catch (e: Exception) {
            logger.error("Job execution failed: $invokeTarget", e)
            throw JobExecutionException(e)
        }
    }
}
