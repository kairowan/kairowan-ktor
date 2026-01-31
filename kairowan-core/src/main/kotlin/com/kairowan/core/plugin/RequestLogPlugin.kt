package com.kairowan.core.framework.web.plugin

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.JsonGenerator
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import org.slf4j.LoggerFactory
import io.ktor.http.*
import io.ktor.http.content.*
import java.nio.charset.StandardCharsets

// 定义为顶层常量，避免每次创建新实例
private val StartTimeKey = AttributeKey<Long>("StartTime")
private val objectMapper = ObjectMapper().apply {
    // 确保使用 UTF-8 编码
    factory.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false)
}

val RequestLogPlugin = createApplicationPlugin(name = "RequestLogPlugin") {
    val logger = LoggerFactory.getLogger("RequestLogPlugin")

    onCall { call ->
        call.attributes.put(StartTimeKey, System.currentTimeMillis())
        val uri = call.request.uri
        val method = call.request.httpMethod.value

        if (!uri.startsWith("/swagger") && !uri.startsWith("/metrics") && !uri.startsWith("/openapi")) {
            val sb = StringBuilder()
            sb.append("Request [").append(method).append(" ").append(uri).append("]")
            
            if (!call.request.queryParameters.isEmpty()) {
                val params = call.request.queryParameters.entries().joinToString(", ") { "${it.key}=[${it.value.joinToString()}]" }
                sb.append(" Params: {").append(params).append("}")
            }

            try {
                val contentType = call.request.contentType()
                if (!contentType.match(ContentType.MultiPart.Any)) {
                    val body = call.receiveText()
                    if (body.isNotBlank()) {
                         sb.append(" Body: ").append(body.replace("\n", "").replace("\r", ""))
                    }
                } else {
                    sb.append(" Body: [Multipart Content]")
                }
            } catch (e: Exception) {
                logger.debug("Failed to read request body: ${e.message}")
            }
            logger.info(sb.toString())
        }
    }

    // 捕获响应并记录日志
    onCallRespond { call, body ->
        val uri = call.request.uri
        if (!uri.startsWith("/swagger") && !uri.startsWith("/metrics") && !uri.startsWith("/openapi")) {
            if (body::class.simpleName == "UnauthorizedResponse") {
                return@onCallRespond
            }
            
            val startTime = call.attributes.getOrNull(StartTimeKey) ?: System.currentTimeMillis()
            val duration = System.currentTimeMillis() - startTime
            val status = call.response.status() ?: HttpStatusCode.OK
            
            // 序列化响应体为 JSON
            val content: String = when {
                body !is OutgoingContent -> {
                    try {
                        objectMapper.writeValueAsString(body)
                    } catch (e: Exception) {
                        body.toString()
                    }
                }
                body is TextContent -> body.text
                body is ByteArrayContent -> "[Binary Data ${body.contentLength ?: "?"} bytes]"
                else -> "[${body::class.simpleName}]"
            }
            
            logger.info("Response [${call.request.httpMethod.value} $uri] (${status.value} ${status.description}) in ${duration}ms: $content")
        }
    }
}
