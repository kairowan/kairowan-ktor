package com.kairowan.monitor.api

/**
 * 监控模块接口路由常量
 */
object MonitorApiRoutes {
    object Monitor {
        const val ROOT = "/monitor"
        const val ONLINE = "/online"
        const val ONLINE_LIST = "/list"
        const val ONLINE_COUNT = "/count"
        const val ONLINE_FORCE = "/{tokenId}"
        const val SERVER = "/server"

        const val ROOT_FULL = ROOT
        const val ONLINE_LIST_FULL = ROOT + ONLINE + ONLINE_LIST
        const val ONLINE_COUNT_FULL = ROOT + ONLINE + ONLINE_COUNT
        const val ONLINE_FORCE_FULL = ROOT + ONLINE + ONLINE_FORCE
        const val SERVER_FULL = ROOT + SERVER
    }

    object Job {
        const val ROOT = "/job"
        const val LIST = "/list"
        const val DETAIL = "/{jobId}"
        const val RUNNING = "/running"
        const val PAUSE = "/pause/{jobId}"
        const val RESUME = "/resume/{jobId}"
        const val RUN = "/run/{jobId}"

        const val LIST_FULL = Monitor.ROOT + ROOT + LIST
        const val DETAIL_FULL = Monitor.ROOT + ROOT + DETAIL
        const val RUNNING_FULL = Monitor.ROOT + ROOT + RUNNING
        const val PAUSE_FULL = Monitor.ROOT + ROOT + PAUSE
        const val RESUME_FULL = Monitor.ROOT + ROOT + RESUME
        const val RUN_FULL = Monitor.ROOT + ROOT + RUN
    }

    object OperLog {
        const val ROOT = "/operlog"
        const val LIST = "/list"
        const val DETAIL = "/{operId}"
        const val CLEAN = "/clean"

        const val LIST_FULL = Monitor.ROOT + ROOT + LIST
        const val DETAIL_FULL = Monitor.ROOT + ROOT + DETAIL
        const val CLEAN_FULL = Monitor.ROOT + ROOT + CLEAN
    }

    object LoginInfo {
        const val ROOT = "/logininfor"
        const val LIST = "/list"
        const val CLEAN = "/clean"

        const val LIST_FULL = Monitor.ROOT + ROOT + LIST
        const val CLEAN_FULL = Monitor.ROOT + ROOT + CLEAN
    }

    object Cache {
        const val ROOT = "/monitor/cache"
        const val STATS = "/stats"
        const val SIZE = "/size"
        const val CLEAR = "/clear"
        const val INFO = "/info"

        const val STATS_FULL = ROOT + STATS
        const val SIZE_FULL = ROOT + SIZE
        const val CLEAR_FULL = ROOT + CLEAR
        const val INFO_FULL = ROOT + INFO
    }

    object Dashboard {
        const val ROOT = "/dashboard"
        const val STATS = "/stats"
        const val SYSTEM_INFO = "/system/info"

        const val STATS_FULL = ROOT + STATS
        const val SYSTEM_INFO_FULL = ROOT + SYSTEM_INFO
    }

    object Analysis {
        const val ROOT = "/analysis"
        const val OVERVIEW = "/overview"
        const val SALES_TREND = "/sales/trend"
        const val CATEGORY = "/category"
        const val REGION = "/region"
        const val USER_GROWTH = "/user/growth"
        const val EXPORT = "/export"

        const val OVERVIEW_FULL = ROOT + OVERVIEW
        const val SALES_TREND_FULL = ROOT + SALES_TREND
        const val CATEGORY_FULL = ROOT + CATEGORY
        const val REGION_FULL = ROOT + REGION
        const val USER_GROWTH_FULL = ROOT + USER_GROWTH
        const val EXPORT_FULL = ROOT + EXPORT
    }
}
