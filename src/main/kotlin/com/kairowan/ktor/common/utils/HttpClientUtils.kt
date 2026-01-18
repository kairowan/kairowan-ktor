package com.kairowan.ktor.common.utils

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*

/**
 * HTTP 客户端工具类
 * @author Kairowan
 * @date 2026-01-18
 */
object HttpClientUtils {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson()
        }
        engine {
            requestTimeout = 30000
        }
    }

    /**
     * GET 请求
     */
    suspend fun get(url: String, headers: Map<String, String> = emptyMap()): String {
        return client.get(url) {
            headers.forEach { (key, value) ->
                header(key, value)
            }
        }.bodyAsText()
    }

    /**
     * POST 请求 (JSON)
     */
    suspend fun postJson(url: String, body: Any, headers: Map<String, String> = emptyMap()): String {
        return client.post(url) {
            contentType(ContentType.Application.Json)
            headers.forEach { (key, value) ->
                header(key, value)
            }
            setBody(body)
        }.bodyAsText()
    }

    /**
     * POST 请求 (Form)
     */
    suspend fun postForm(url: String, params: Map<String, String>, headers: Map<String, String> = emptyMap()): String {
        return client.post(url) {
            contentType(ContentType.Application.FormUrlEncoded)
            headers.forEach { (key, value) ->
                header(key, value)
            }
            setBody(params.entries.joinToString("&") { "${it.key}=${it.value}" })
        }.bodyAsText()
    }

    /**
     * PUT 请求
     */
    suspend fun put(url: String, body: Any, headers: Map<String, String> = emptyMap()): String {
        return client.put(url) {
            contentType(ContentType.Application.Json)
            headers.forEach { (key, value) ->
                header(key, value)
            }
            setBody(body)
        }.bodyAsText()
    }

    /**
     * DELETE 请求
     */
    suspend fun delete(url: String, headers: Map<String, String> = emptyMap()): String {
        return client.delete(url) {
            headers.forEach { (key, value) ->
                header(key, value)
            }
        }.bodyAsText()
    }

    /**
     * 关闭客户端
     */
    fun close() {
        client.close()
    }
}
