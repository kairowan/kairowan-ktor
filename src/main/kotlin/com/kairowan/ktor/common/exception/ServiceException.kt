package com.kairowan.ktor.common.exception

import com.kairowan.ktor.common.constant.ResultCode

/**
 * 业务异常
 */
class ServiceException(
    val code: Int = 500,
    override val message: String
) : RuntimeException(message) {
    
    constructor(resultCode: ResultCode) : this(resultCode.code, resultCode.msg)
}

