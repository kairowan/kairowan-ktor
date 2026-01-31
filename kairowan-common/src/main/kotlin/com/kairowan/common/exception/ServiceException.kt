package com.kairowan.common.exception

import com.kairowan.common.constant.ResultCode

/**
 * 业务异常
 */
class ServiceException(
    val code: Int = 500,
    override val message: String
) : RuntimeException(message) {
    
    constructor(resultCode: ResultCode) : this(resultCode.code, resultCode.msg)
}

