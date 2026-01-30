package com.kairowan.ktor.framework.web.service

import com.kairowan.ktor.core.database.DatabaseProvider
import com.kairowan.ktor.core.scheduling.TaskScheduler
import com.kairowan.ktor.framework.web.domain.*
import com.kairowan.ktor.framework.web.page.KPageRequest
import com.kairowan.ktor.framework.web.page.KTableData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.dsl.*
import org.ktorm.entity.add
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.update
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import org.quartz.impl.matchers.GroupMatcher
import org.slf4j.LoggerFactory

/**
 * 定时任务服务 (框架层 - 保留兼容性)
 * @author Kairowan
 * @date 2026-01-18
 */
class SysJobService(
    private val databaseProvider: DatabaseProvider,
    private val taskScheduler: TaskScheduler
) {
    private val database get() = databaseProvider.database
    private val logger = LoggerFactory.getLogger(SysJobService::class.java)
    private val scheduler: Scheduler = StdSchedulerFactory.getDefaultScheduler()

    companion object {
        const val JOB_GROUP = "DEFAULT"
    }

    /**
     * 获取任务列表
     */
    suspend fun list(page: KPageRequest? = null): KTableData = withContext(Dispatchers.IO) {
        val safePage = page?.normalized()
        val query = database.from(SysJobs)
            .select()
            .orderBy(SysJobs.jobId.asc())

        if (safePage != null) {
            val offset = safePage.getOffset()
            query.limit(offset, safePage.pageSize)
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
        taskScheduler.removeJob(job.jobName, job.jobGroup)
        
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
        taskScheduler.removeJob(job.jobName, job.jobGroup)
        
        database.delete(SysJobs) { it.jobId eq jobId }
        true
    }

    /**
     * 暂停任务
     */
    suspend fun pauseJob(jobId: Long): Boolean = withContext(Dispatchers.IO) {
        val job = database.sequenceOf(SysJobs).find { it.jobId eq jobId } ?: return@withContext false
        
        taskScheduler.pauseJob(job.jobName, job.jobGroup)
        
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
        
        if (!taskScheduler.checkExists(job.jobName, job.jobGroup)) {
            addToScheduler(job)
        } else {
            taskScheduler.resumeJob(job.jobName, job.jobGroup)
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
        
        if (!taskScheduler.checkExists(job.jobName, job.jobGroup)) {
            addToScheduler(job)
        }
        
        taskScheduler.triggerJob(job.jobName, job.jobGroup)
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
            val jobData = mapOf(
                "invokeTarget" to job.invokeTarget,
                "jobId" to job.jobId
            )
            taskScheduler.addJob(
                job.jobName,
                job.jobGroup,
                GenericJob::class.java,
                job.cronExpression,
                jobData
            )
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
 */
class GenericJob : Job {
    private val logger = LoggerFactory.getLogger(GenericJob::class.java)
    
    override fun execute(context: JobExecutionContext) {
        val dataMap = context.jobDetail.jobDataMap
        val invokeTarget = dataMap.getString("invokeTarget")
        val jobId = dataMap.getLong("jobId")
        
        logger.info("Executing job: $invokeTarget (ID: $jobId)")
        
        try {
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
