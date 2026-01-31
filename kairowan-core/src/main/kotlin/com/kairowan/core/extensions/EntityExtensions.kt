package com.kairowan.core.extensions

import org.ktorm.entity.Entity
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime

/**
 * Ktorm Entity 扩展函数
 * 用于解决Entity序列化时包含properties字段的问题
 *
 * @author Kairowan
 * @date 2026-01-29
 */

/**
 * 将Entity转换为Map，避免序列化properties字段
 * 使用反射获取所有属性，并将日期时间类型转换为字符串
 */
fun <E : Entity<E>> E.toMap(): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()

    // 获取Entity的所有属性
    this.properties.forEach { (name, value) ->
        // 转换日期时间类型为String
        val convertedValue = when (value) {
            is LocalDateTime -> value.toString()
            is LocalDate -> value.toString()
            is LocalTime -> value.toString()
            else -> value
        }
        map[name] = convertedValue
    }

    return map
}

/**
 * 将Entity列表转换为Map列表
 */
fun <E : Entity<E>> List<E>.toMapList(): List<Map<String, Any?>> {
    return this.map { it.toMap() }
}
