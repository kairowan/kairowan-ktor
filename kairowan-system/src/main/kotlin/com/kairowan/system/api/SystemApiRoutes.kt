package com.kairowan.system.api

/**
 * 系统模块接口路由常量
 *
 * @author Kairowan
 * @date 2026-01-30
 */
object SystemApiRoutes {
    object Auth {
        const val ROOT = "/auth"
        const val LOGIN = "/login"
        const val LOGOUT = "/logout"
        const val GET_INFO = "/getInfo"
        const val GET_ROUTERS = "/getRouters"

        const val LOGIN_FULL = ROOT + LOGIN
        const val LOGOUT_FULL = ROOT + LOGOUT
        const val GET_INFO_FULL = ROOT + GET_INFO
        const val GET_ROUTERS_FULL = ROOT + GET_ROUTERS
    }

    object Notification {
        const val ROOT = "/system/notification"
        const val LIST = "/list"
        const val STATS = "/stats"
        const val READ = "/read"
        const val READ_ALL = "/read/all"
        const val READ_ONE = "/read/{id}"
        const val UNREAD_COUNT = "/unread/count"
        const val DELETE_ONE = "/{id}"

        const val LIST_FULL = ROOT + LIST
        const val STATS_FULL = ROOT + STATS
        const val READ_FULL = ROOT + READ
        const val READ_ALL_FULL = ROOT + READ_ALL
        const val READ_ONE_FULL = ROOT + READ_ONE
        const val UNREAD_COUNT_FULL = ROOT + UNREAD_COUNT
        const val DELETE_ONE_FULL = ROOT + DELETE_ONE
    }

    object Captcha {
        const val IMAGE = "/captchaImage"
    }

    object Profile {
        const val ROOT = "/profile"
        const val INFO = "/info"
        const val UPDATE = "/update"
        const val PASSWORD = "/password"
        const val AVATAR = "/avatar"
        const val LOGS = "/logs"

        const val INFO_FULL = ROOT + INFO
        const val UPDATE_FULL = ROOT + UPDATE
        const val PASSWORD_FULL = ROOT + PASSWORD
        const val AVATAR_FULL = ROOT + AVATAR
        const val LOGS_FULL = ROOT + LOGS
    }

    object File {
        const val ROOT = "/file"
        const val LIST = "/list"
        const val STATS = "/stats"
        const val UPLOAD = "/upload"
        const val DOWNLOAD = "/download/{id}"
        const val DELETE_ONE = "/delete/{id}"
        const val DELETE_BATCH = "/batch/delete"

        const val LIST_FULL = ROOT + LIST
        const val STATS_FULL = ROOT + STATS
        const val UPLOAD_FULL = ROOT + UPLOAD
        const val DOWNLOAD_FULL = ROOT + DOWNLOAD
        const val DELETE_ONE_FULL = ROOT + DELETE_ONE
        const val DELETE_BATCH_FULL = ROOT + DELETE_BATCH
    }

    object ToolFile {
        const val ROOT = "/tool/file"
        const val SYNC = "/sync"
        const val UPLOAD = "/upload"
        const val LIST = "/list"
        const val DOWNLOAD = "/download/{id}"
        const val DELETE_ONE = "/{id}"
        const val DELETE_BATCH = "/batch"

        const val SYNC_FULL = ROOT + SYNC
        const val UPLOAD_FULL = ROOT + UPLOAD
        const val LIST_FULL = ROOT + LIST
        const val DOWNLOAD_FULL = ROOT + DOWNLOAD
        const val DELETE_ONE_FULL = ROOT + DELETE_ONE
        const val DELETE_BATCH_FULL = ROOT + DELETE_BATCH
    }

    object User {
        const val ROOT = "/system/user"
        const val LIST = "/list"
        const val DETAIL = "/{userId}"
        const val EXPORT = "/export"

        const val LIST_FULL = ROOT + LIST
        const val DETAIL_FULL = ROOT + DETAIL
        const val EXPORT_FULL = ROOT + EXPORT
    }

    object Role {
        const val ROOT = "/system/role"
        const val LIST = "/list"
        const val DELETE_ONE = "/{roleId}"

        const val LIST_FULL = ROOT + LIST
        const val DELETE_ONE_FULL = ROOT + DELETE_ONE
    }

    object Menu {
        const val ROOT = "/system/menu"
        const val LIST = "/list"
        const val DELETE_ONE = "/{menuId}"

        const val LIST_FULL = ROOT + LIST
        const val DELETE_ONE_FULL = ROOT + DELETE_ONE
    }

    object Dept {
        const val ROOT = "/system/dept"
        const val LIST = "/list"
        const val TREE_SELECT = "/treeselect"
        const val DETAIL = "/{deptId}"
        const val DELETE_ONE = "/{deptId}"

        const val LIST_FULL = ROOT + LIST
        const val TREE_SELECT_FULL = ROOT + TREE_SELECT
        const val DETAIL_FULL = ROOT + DETAIL
        const val DELETE_ONE_FULL = ROOT + DELETE_ONE
    }

    object Post {
        const val ROOT = "/system/post"
        const val LIST = "/list"
        const val OPTION_SELECT = "/optionselect"
        const val DETAIL = "/{postId}"
        const val DELETE_ONE = "/{postId}"

        const val LIST_FULL = ROOT + LIST
        const val OPTION_SELECT_FULL = ROOT + OPTION_SELECT
        const val DETAIL_FULL = ROOT + DETAIL
        const val DELETE_ONE_FULL = ROOT + DELETE_ONE
    }

    object Config {
        const val ROOT = "/system/config"
        const val LIST = "/list"
        const val GET_BY_KEY = "/configKey/{configKey}"
        const val REFRESH_CACHE = "/refreshCache"

        const val LIST_FULL = ROOT + LIST
        const val GET_BY_KEY_FULL = ROOT + GET_BY_KEY
        const val REFRESH_CACHE_FULL = ROOT + REFRESH_CACHE
    }

    object Dict {
        const val ROOT = "/system/dict"
        const val TYPE_LIST = "/type/list"
        const val DATA_BY_TYPE = "/data/type/{dictType}"
        const val REFRESH_CACHE = "/refreshCache"

        const val TYPE_LIST_FULL = ROOT + TYPE_LIST
        const val DATA_BY_TYPE_FULL = ROOT + DATA_BY_TYPE
        const val REFRESH_CACHE_FULL = ROOT + REFRESH_CACHE
    }
}
