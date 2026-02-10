package com.kairowan.app.lifecycle

import io.ktor.util.AttributeKey
import io.micrometer.prometheus.PrometheusMeterRegistry

internal const val TOKEN_BLACKLIST_PREFIX = "login_token:blacklist:"
internal val PrometheusRegistryKey = AttributeKey<PrometheusMeterRegistry>("PrometheusRegistry")
