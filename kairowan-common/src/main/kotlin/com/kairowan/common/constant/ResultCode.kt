package com.kairowan.common.constant

/**
 * 统一状态码定义
 * Standard Result Codes
 * 
 * @author Kairowan
 * @date 2026-01-17
 */
enum class ResultCode(val code: Int, val msg: String) {
    SUCCESS(200, "操作成功"),
    
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "账号未登录"),
    FORBIDDEN(403, "没有权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    
    ERROR(500, "系统内部错误"),
    NOT_IMPLEMENTED(501, "功能未实现"),
    
    USER_PASSWORD_ERROR(601, "用户名或密码错误"),
    USER_NOT_EXISTS(602, "用户不存在"),
    USER_LOCKED(603, "用户已被锁定"),
    USER_DISABLED(604, "用户已停用"),
    USER_PASSWORD_NOT_MATCH(605, "密码错误"),
    EXISTS(606, "数据已存在");
    
    companion object {
        fun valueOf(code: Int): ResultCode? = entries.find { it.code == code }
    }
}
