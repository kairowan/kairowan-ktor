package com.kairowan.common.utils

/**
 * Excel 导出注解
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Excel(
    val name: String = "",
    val dateFormat: String = "yyyy-MM-dd HH:mm:ss"
)
