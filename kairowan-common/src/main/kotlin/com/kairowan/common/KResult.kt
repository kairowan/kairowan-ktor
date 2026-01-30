package com.kairowan.ktor.common

import com.kairowan.ktor.common.constant.ResultCode
import java.io.Serializable

/**
 * 通用返回实体 (KResult)
 * Kairowan Enterprise Response Wrapper
 *
 * @author Kairowan
 * @date 2026-01-17
 */
data class KResult<T>(
    val code: Int,
    val msg: String,
    val data: T? = null
) : Serializable {
    companion object {
        
        fun <T> ok(data: T? = null): KResult<T> {
            return KResult(ResultCode.SUCCESS.code, ResultCode.SUCCESS.msg, data)
        }
        
        fun <T> ok(msg: String, data: T? = null): KResult<T> {
            return KResult(ResultCode.SUCCESS.code, msg, data)
        }

        fun <T> fail(msg: String): KResult<T> {
            return KResult(ResultCode.ERROR.code, msg, null)
        }

        fun <T> fail(code: Int, msg: String): KResult<T> {
            return KResult(code, msg, null)
        }
        
        fun <T> fail(resultCode: ResultCode): KResult<T> {
            return KResult(resultCode.code, resultCode.msg, null)
        }
    }
}
