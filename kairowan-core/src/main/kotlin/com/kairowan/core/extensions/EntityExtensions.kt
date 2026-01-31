package com.kairowan.core.extensions

import org.ktorm.entity.Entity
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime

/**
 * Ktorm Entity 扩展函数
 *
 * @author Kairowan
 * @date 2026-01-29
 */

fun <E : Entity<E>> E.toMap(): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()

    this.properties.forEach { (name, value) ->
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

fun <E : Entity<E>> List<E>.toMapList(): List<Map<String, Any?>> {
    return this.map { it.toMap() }
}
