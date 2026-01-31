package com.kairowan.core.framework.security

import io.ktor.server.auth.*

data class LoginUser(
    val userId: Int,
    val username: String,
    val user: Any? = null,
    val roles: Set<String> = emptySet(),
    val permissions: Set<String> = emptySet()
) : Principal
