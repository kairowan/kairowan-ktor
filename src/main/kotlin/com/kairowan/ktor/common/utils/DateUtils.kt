package com.kairowan.ktor.common.utils

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 日期工具类
 * @author Kairowan
 * @date 2026-01-18
 */
object DateUtils {
    
    const val YYYY = "yyyy"
    const val YYYY_MM = "yyyy-MM"
    const val YYYY_MM_DD = "yyyy-MM-dd"
    const val YYYYMMDDHHMMSS = "yyyyMMddHHmmss"
    const val YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss"
    
    private val formatters = mutableMapOf<String, DateTimeFormatter>()
    
    private fun getFormatter(pattern: String): DateTimeFormatter {
        return formatters.getOrPut(pattern) { DateTimeFormatter.ofPattern(pattern) }
    }

    /**
     * 获取当前日期时间
     */
    fun now(): LocalDateTime = LocalDateTime.now()
    
    /**
     * 获取当前日期时间字符串
     */
    fun nowStr(pattern: String = YYYY_MM_DD_HH_MM_SS): String {
        return now().format(getFormatter(pattern))
    }
    
    /**
     * 格式化日期
     */
    fun format(dateTime: LocalDateTime, pattern: String = YYYY_MM_DD_HH_MM_SS): String {
        return dateTime.format(getFormatter(pattern))
    }
    
    /**
     * 解析日期字符串
     */
    fun parse(dateStr: String, pattern: String = YYYY_MM_DD_HH_MM_SS): LocalDateTime {
        return LocalDateTime.parse(dateStr, getFormatter(pattern))
    }
    
    /**
     * LocalDateTime 转 Date
     */
    fun toDate(dateTime: LocalDateTime): Date {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant())
    }
    
    /**
     * Date 转 LocalDateTime
     */
    fun toLocalDateTime(date: Date): LocalDateTime {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
    }
    
    /**
     * 获取两个日期之间的天数差
     */
    fun daysBetween(start: LocalDateTime, end: LocalDateTime): Long {
        return java.time.Duration.between(start, end).toDays()
    }
}
