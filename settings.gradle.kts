rootProject.name = "kairowan-ktor"

// 包含所有子模块
include(
    ":kairowan-common",      // 公共工具模块
    ":kairowan-core",        // 核心框架模块
    ":kairowan-system",      // 系统管理模块
    ":kairowan-monitor",     // 监控模块
    ":kairowan-generator",   // 代码生成器模块
    ":kairowan-app"          // 应用启动模块
)
