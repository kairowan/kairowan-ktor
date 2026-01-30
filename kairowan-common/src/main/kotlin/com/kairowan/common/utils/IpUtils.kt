package com.kairowan.ktor.common.utils

import io.ktor.server.request.*

/**
 * IP 工具类
 * @author Kairowan
 * @date 2026-01-18
 */
object IpUtils {

    /**
     * 从请求中获取客户端 IP 地址
     */
    fun getClientIp(request: ApplicationRequest): String {
        // 优先从代理头获取
        val xForwardedFor = request.headers["X-Forwarded-For"]
        if (!xForwardedFor.isNullOrBlank()) {
            // X-Forwarded-For 可能包含多个IP，取第一个
            return xForwardedFor.split(",").first().trim()
        }
        
        val xRealIp = request.headers["X-Real-IP"]
        if (!xRealIp.isNullOrBlank()) {
            return xRealIp.trim()
        }
        
        val proxyClientIp = request.headers["Proxy-Client-IP"]
        if (!proxyClientIp.isNullOrBlank() && proxyClientIp != "unknown") {
            return proxyClientIp.trim()
        }
        
        val wlProxyClientIp = request.headers["WL-Proxy-Client-IP"]
        if (!wlProxyClientIp.isNullOrBlank() && wlProxyClientIp != "unknown") {
            return wlProxyClientIp.trim()
        }
        
        // 从连接获取
        return request.local.remoteHost
    }

    /**
     * 判断是否为内网IP
     */
    fun isInternalIp(ip: String): Boolean {
        if (ip == "127.0.0.1" || ip == "localhost" || ip == "0:0:0:0:0:0:0:1") {
            return true
        }
        
        val parts = ip.split(".")
        if (parts.size != 4) return false
        
        return try {
            val a = parts[0].toInt()
            val b = parts[1].toInt()
            
            when {
                a == 10 -> true                          // 10.x.x.x
                a == 172 && b in 16..31 -> true          // 172.16.x.x - 172.31.x.x
                a == 192 && b == 168 -> true             // 192.168.x.x
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取IP归属地 (占位，可集成第三方服务)
     */
    fun getIpLocation(ip: String): String {
        return if (isInternalIp(ip)) "内网IP" else "未知"
    }
}
