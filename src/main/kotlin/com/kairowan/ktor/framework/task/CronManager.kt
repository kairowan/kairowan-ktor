package com.kairowan.ktor.framework.task

import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import org.slf4j.LoggerFactory
import java.util.*

/**
 * 定时任务管理器 (基于 Quartz)
 *
 * @author Kairowan
 * @date 2026-01-17
 */
object CronManager {
    private val logger = LoggerFactory.getLogger(CronManager::class.java)
    private val scheduler: Scheduler = StdSchedulerFactory.getDefaultScheduler()

    fun start() {
        if (!scheduler.isStarted) {
            scheduler.start()
            logger.info("Quartz Scheduler started.")
        }
    }

    fun shutdown() {
        if (scheduler.isStarted) {
            scheduler.shutdown(true)
        }
    }

    /**
     * 添加具体任务
     * @param jobClass 任务类 (必须实现 org.quartz.Job)
     * @param cronExpression CRON 表达式
     */
    fun addJob(jobName: String, jobClass: Class<out Job>, cronExpression: String) {
        val jobDetail = JobBuilder.newJob(jobClass)
            .withIdentity(jobName, "DEFAULT")
            .build()

        val trigger = TriggerBuilder.newTrigger()
            .withIdentity("$jobName-Trigger", "DEFAULT")
            .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
            .build()

        scheduler.scheduleJob(jobDetail, trigger)
        logger.info("Added job: $jobName with CLI: $cronExpression")
    }
}
