package com.kairowan.common.constant

/**
 * 缓存常量
 */
object CacheConstants {
    // 用户相关缓存
    const val USER_PERMISSIONS_PREFIX = "user:permissions:"
    const val USER_ROLES_PREFIX = "user:roles:"
    const val USER_MENU_TREE_PREFIX = "user:menu:tree:"
    const val USER_INFO_PREFIX = "user:info:"

    // 系统配置缓存前缀
    const val CONFIG_PREFIX = "config:"

    // 数据字典缓存前缀
    const val DICT_PREFIX = "dict:"

    // 验证码缓存前缀
    const val CAPTCHA_PREFIX = "captcha:"

    // Token 黑名单前缀
    const val TOKEN_BLACKLIST_PREFIX = "token:blacklist:"

    // 在线用户前缀
    const val ONLINE_USER_PREFIX = "online:user:"

    // 登录尝试次数前缀
    const val LOGIN_ATTEMPTS_PREFIX = "login:attempts:"

    // 登录锁定前缀
    const val LOGIN_LOCKED_PREFIX = "login:locked:"

    // 默认缓存过期时间（秒）
    const val DEFAULT_EXPIRE_TIME = 3600  // 1小时
    const val SHORT_EXPIRE_TIME = 300     // 5分钟
    const val LONG_EXPIRE_TIME = 86400    // 24小时
}
