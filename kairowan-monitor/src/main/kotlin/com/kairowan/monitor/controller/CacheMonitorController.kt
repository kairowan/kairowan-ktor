package com.kairowan.monitor.controller

import com.kairowan.common.KResult
import com.kairowan.core.cache.TwoLevelCacheProvider
import com.kairowan.core.controller.AuthenticatedRouteController
import com.kairowan.core.controller.KController
import com.kairowan.core.framework.cache.CacheProvider
import com.kairowan.core.framework.security.requirePermission
import com.kairowan.monitor.api.MonitorApiRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

/**
 * 缓存监控路由
 * Cache Monitor Routes
 *
 * @author Kairowan
 * @date 2026-01-28
 */
class CacheMonitorController : KController(), AuthenticatedRouteController {

    override fun register(route: Route) {
        route.routes()
    }

    private fun Route.routes() {
        val cacheProvider by inject<CacheProvider>()

        route(MonitorApiRoutes.Cache.ROOT) {
            requirePermission("monitor:cache:list") {
                get(MonitorApiRoutes.Cache.STATS) {
                    if (cacheProvider is TwoLevelCacheProvider) {
                        val stats = (cacheProvider as TwoLevelCacheProvider).getL1Stats()
                        call.respond(KResult.ok(stats))
                    } else {
                        call.respond(KResult.ok(mapOf(
                            "message" to "Two-level cache not enabled"
                        )))
                    }
                }
            }

            requirePermission("monitor:cache:list") {
                get(MonitorApiRoutes.Cache.SIZE) {
                    if (cacheProvider is TwoLevelCacheProvider) {
                        val size = (cacheProvider as TwoLevelCacheProvider).getL1Size()
                        call.respond(KResult.ok(mapOf(
                            "l1Size" to size
                        )))
                    } else {
                        call.respond(KResult.ok(mapOf(
                            "message" to "Two-level cache not enabled"
                        )))
                    }
                }
            }

            // 清空 L1 缓存
            requirePermission("monitor:cache:clear") {
                post(MonitorApiRoutes.Cache.CLEAR) {
                    if (cacheProvider is TwoLevelCacheProvider) {
                        (cacheProvider as TwoLevelCacheProvider).clearL1()
                        call.respond(KResult.ok<String>("L1 cache cleared"))
                    } else {
                        call.respond(KResult.fail<String>("Two-level cache not enabled"))
                    }
                }
            }

            requirePermission("monitor:cache:list") {
                get(MonitorApiRoutes.Cache.INFO) {
                    if (cacheProvider is TwoLevelCacheProvider) {
                        val stats = (cacheProvider as TwoLevelCacheProvider).getL1Stats()
                        val info = mapOf(
                            "type" to "TwoLevelCache",
                            "l1" to mapOf(
                                "type" to "Caffeine",
                                "size" to stats.size,
                                "hitCount" to stats.hitCount,
                                "missCount" to stats.missCount,
                                "hitRate" to String.format("%.2f%%", stats.hitRate * 100),
                                "missRate" to String.format("%.2f%%", stats.missRate * 100),
                                "evictionCount" to stats.evictionCount
                            ),
                            "l2" to mapOf(
                                "type" to "Redis",
                                "status" to "connected"
                            )
                        )
                        call.respond(KResult.ok(info))
                    } else {
                        call.respond(KResult.ok(mapOf(
                            "type" to "SingleLevel",
                            "cache" to "Redis"
                        )))
                    }
                }
            }
        }
    }
}
