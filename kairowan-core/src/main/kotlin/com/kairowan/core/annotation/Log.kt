package com.kairowan.core.framework.web.annotation

/**
 * 操作日志注解
 * 用于标记需要记录操作日志的方法
 * 
 * @author Kairowan
 * @date 2026-01-18
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Log(
    /** 模块标题 */
    val title: String = "",
    
    /** 业务类型 (0=其它 1=新增 2=修改 3=删除 4=查询 5=导出 6=导入) */
    val businessType: BusinessType = BusinessType.OTHER,
    
    /** 是否保存请求参数 */
    val isSaveRequestData: Boolean = true,
    
    /** 是否保存响应数据 */
    val isSaveResponseData: Boolean = true
)

/**
 * 业务类型枚举
 */
enum class BusinessType(val code: Int, val desc: String) {
    OTHER(0, "其它"),
    INSERT(1, "新增"),
    UPDATE(2, "修改"),
    DELETE(3, "删除"),
    QUERY(4, "查询"),
    EXPORT(5, "导出"),
    IMPORT(6, "导入"),
    GRANT(7, "授权"),
    FORCE(8, "强退"),
    CLEAN(9, "清空")
}
