# Kairowan-Ktor 企业级脚手架 (多模块版)

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-1.9.23-blue.svg" alt="Kotlin">
  <img src="https://img.shields.io/badge/Ktor-2.3.9-orange.svg" alt="Ktor">
  <img src="https://img.shields.io/badge/Architecture-Multi--Module-green.svg" alt="Architecture">
  <img src="https://img.shields.io/badge/License-MIT-green.svg" alt="License">
</p>

基于 **Kotlin + Ktor** 构建的企业级后台管理系统脚手架，采用**多模块化架构**，开箱即用，功能完整。

> 🎉 **v2.0 重大更新**: 项目已重构为多模块架构，提升编译速度、代码复用性和团队协作效率！

## 🔗 相关项目

- **后端 API**: [kairowan-ktor](https://github.com/kairowan/kairowan-ktor) (当前项目)
- **前端管理系统**: [kairowan-admin](https://github.com/kairowan/kairowan-admin) - 基于 Vue 3 + Element Plus 的后台管理界面

---

## 🏗️ 多模块架构

```
kairowan-ktor/
├── kairowan-common      # 公共工具模块 (零依赖) - 204KB
├── kairowan-core        # 核心框架模块 (安全、缓存、数据库) - 1.6MB
├── kairowan-system      # 系统管理模块 (用户、角色、菜单) - 288KB
├── kairowan-monitor     # 监控模块 (日志、任务、服务器监控) - 92KB
├── kairowan-generator   # 代码生成器模块 - 1.1MB
└── kairowan-app         # 应用启动模块 - 204KB
```

**架构优势**:
- ✅ **编译速度提升 50%+** - 增量编译 + 并行编译
- ✅ **代码复用** - 各模块可独立发布和引用
- ✅ **团队协作** - 不同团队负责不同模块，减少冲突
- ✅ **独立测试** - 每个模块可单独测试
- ✅ **按需部署** - 灵活选择部署哪些模块

---

## ✨ 功能特性

### 🔐 认证与权限
- [x] JWT Token 认证 (HMAC256 算法)
- [x] 登录/登出 (Token 黑名单机制)
- [x] 图片验证码 (Redis 存储)
- [x] RBAC 权限控制 (角色/菜单/权限)
- [x] 接口权限注解 (`requirePermission`)
- [x] 在线用户管理 (强制踢出)

### 📊 系统管理
- [x] 用户管理 CRUD (支持导出 Excel)
- [x] 角色管理 CRUD
- [x] 菜单管理 CRUD (动态路由)
- [x] 部门管理 (树形结构)
- [x] 岗位管理
- [x] 系统配置 (Redis 缓存)
- [x] 数据字典 (Redis 缓存)
- [x] 个人中心 (资料修改、密码修改、头像上传)
- [x] 通知管理

### 📝 日志与监控
- [x] 操作日志自动记录 (Plugin)
- [x] 登录日志
- [x] 服务器监控 (CPU/内存/JVM/磁盘)
- [x] 缓存监控 (两级缓存统计)
- [x] 仪表盘 (数据统计)
- [x] 数据分析

### 📁 文件管理
- [x] 本地文件存储
- [x] MinIO/OSS 集成 (可选)
- [x] 文件上传记录
- [x] 预签名 URL 预览
- [x] 文件类型自动识别
- [x] 启动时文件同步 (可配置)

### ⏰ 定时任务
- [x] Quartz 在线管理
- [x] 任务 CRUD/暂停/恢复
- [x] 立即执行一次

### 🔌 WebSocket
- [x] 实时消息推送
- [x] 用户专属通道
- [x] 广播消息
- [x] HTTP API 推送

### 🛡️ 安全增强
- [x] 接口幂等控制 (防重复提交)
- [x] 请求限流 (RateLimit: 100次/分钟)
- [x] 参数校验 (RequestValidation)
- [x] CORS 跨域配置
- [x] 密码加密 (BCrypt)

### 🧰 开发工具
- [x] 代码生成器 (自动生成 Entity/Service/Controller)
- [x] Swagger UI 接口文档
- [x] Prometheus 指标监控
- [x] 健康检查 (/health, /ready)

### ⚡ 性能优化
- [x] 两级缓存 (Caffeine + Redis)
  - L1: Caffeine 本地缓存 (5000个key，5分钟过期)
  - L2: Redis 分布式缓存
- [x] HikariCP 连接池优化
- [x] MySQL 连接参数优化 (预编译语句缓存、批量操作重写)
- [x] Flyway 数据库迁移 (支持 runOnce 模式)
- [x] 数据库预热 (可配置)
- [x] 文件同步优化 (支持 syncOnce 模式)

---

## 🏗️ 技术栈

| 类别 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 语言 | Kotlin | 1.9.23 | JVM 目标版本 17 |
| 框架 | Ktor | 2.3.9 | 异步 Web 框架 |
| 数据库 | MySQL | 8.0+ | 关系型数据库 |
| ORM | Ktorm | 3.6.0 | Kotlin ORM 框架 |
| 连接池 | HikariCP | 5.1.0 | 高性能连接池 |
| 缓存 | Redis | - | 分布式缓存 |
| 缓存客户端 | Jedis | 5.1.0 | Redis Java 客户端 |
| 本地缓存 | Caffeine | 3.1.8 | 高性能本地缓存 |
| 认证 | JWT | - | Auth0 JWT 库 |
| 加密 | BCrypt | 0.4 | 密码加密 |
| 文件存储 | MinIO | 8.5.7 | 对象存储 (可选) |
| 定时任务 | Quartz | 2.3.2 | 任务调度 |
| DI | Koin | 3.5.3 | 依赖注入 |
| 数据库迁移 | Flyway | 10.4.1 | 版本管理 |
| 文档 | Swagger UI | 2.9.0 | API 文档 |
| 监控 | Micrometer | 1.12.3 | 指标收集 |
| 监控 | Prometheus | - | 指标存储 |
| Excel | Apache POI | 5.2.5 | Excel 处理 |
| 工具库 | Commons Lang3 | 3.14.0 | 通用工具 |

---

## 🚀 快速开始

> 📖 **完整指南**: 查看 [QUICKSTART.md](./QUICKSTART.md) 获取详细的 5 分钟快速启动教程

### 环境要求
- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- MinIO (可选)
- Gradle 8.0+ (或使用 Gradle Wrapper)

### 1. 克隆项目
```bash
# 克隆后端项目
git clone https://github.com/kairowan/kairowan-ktor.git
cd kairowan-ktor

# (可选) 克隆前端项目
git clone https://github.com/kairowan/kairowan-admin.git
```

### 2. 配置数据库

**首先复制配置模板：**
```bash
cp kairowan-app/src/main/resources/application.conf.example kairowan-app/src/main/resources/application.conf
```

**然后编辑 `kairowan-app/src/main/resources/application.conf`，修改以下配置：**

```yaml
db:
  url: "jdbc:mysql://localhost:3306/kairowan_ktor?useUnicode=true&characterEncoding=utf8&useSSL=false&..."
  user: "root"
  password: "your_password"  # 修改为你的数据库密码

  flyway:
    enabled: false  # 生产环境建议禁用
    runOnce: true   # 只在首次启动时迁移

  warmup:
    enabled: false  # 可加快启动速度

redis:
  host: "localhost"
  port: 6379
  password: "your_redis_password"  # 修改为你的 Redis 密码

jwt:
  secret: "your-very-long-and-secure-secret-key-at-least-32-chars"  # 生产环境必须修改

file:
  uploadPath: "uploads"
  urlPrefix: "http://localhost:8080/files"
  syncOnStartup: true  # 启动时同步文件
    syncOnce = true       # 仅首次同步
}
```

### 3. 初始化数据库
```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS kairowan_ktor CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入初始化脚本
mysql -u root -p kairowan_ktor < kairowan-app/src/main/resources/sql/init_complete.sql
```

### 4. 编译项目
```bash
# 编译所有模块
./gradlew build -x test

# 只编译特定模块
./gradlew :kairowan-system:build
```

### 5. 启动项目
```bash
./gradlew :kairowan-app:run
```

### 6. 访问
- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui/index.html
- Prometheus: http://localhost:8080/metrics
- 健康检查: http://localhost:8080/health

### 7. 默认账号
```
用户名: admin
密码: admin123
```

---

## 📚 API 模块

| 模块 | 路径 | 说明 |
|------|------|------|
| 认证 | `/login`, `/logout`, `/getInfo` | 登录登出 |
| 验证码 | `/captchaImage` | 图片验证码 |
| 用户 | `/system/user/**` | 用户 CRUD |
| 角色 | `/system/role/**` | 角色 CRUD |
| 菜单 | `/system/menu/**` | 菜单 CRUD |
| 部门 | `/system/dept/**` | 部门 CRUD |
| 岗位 | `/system/post/**` | 岗位 CRUD |
| 配置 | `/system/config/**` | 系统配置 |
| 字典 | `/system/dict/**` | 数据字典 |
| 文件 | `/tool/file/**` | 文件管理 |
| 个人中心 | `/system/user/profile/**` | 个人资料 |
| 通知 | `/system/notification/**` | 通知管理 |
| 日志 | `/monitor/operlog/**` | 操作日志 |
| 在线 | `/monitor/online/**` | 在线用户 |
| 监控 | `/monitor/server` | 服务器监控 |
| 缓存监控 | `/monitor/cache/**` | 缓存统计 |
| 仪表盘 | `/monitor/dashboard/**` | 数据统计 |
| 分析 | `/monitor/analysis/**` | 数据分析 |
| 任务 | `/monitor/job/**` | 定时任务 |
| 生成 | `/tool/gen/**` | 代码生成 |
| 幂等 | `/common/idempotent/**` | 幂等令牌 |
| WS | `/ws`, `/ws/user/{id}` | WebSocket |
| WS API | `/ws/api/**` | 消息推送 |

---

## 📁 项目结构

```
kairowan-ktor/
├── buildSrc/                        # 统一依赖管理
│   └── src/main/kotlin/
│       └── Dependencies.kt          # 版本号集中管理
│
├── kairowan-common/                 # 公共模块 (零依赖)
│   └── src/main/kotlin/com/kairowan/common/
│       ├── constant/                # 常量定义 (ResultCode, CacheConstants)
│       ├── exception/               # 异常处理 (ServiceException)
│       ├── utils/                   # 工具类
│       │   ├── DateUtils.kt         # 日期时间工具
│       │   ├── SecurityUtils.kt     # 加密工具 (BCrypt, AES)
│       │   ├── IpUtils.kt           # IP 地址工具
│       │   ├── TreeUtils.kt         # 树形结构工具
│       │   ├── HttpClientUtils.kt   # HTTP 客户端工具
│       │   ├── ExcelUtils.kt        # Excel 导入导出
│       │   └── FileUploadUtils.kt   # 文件上传工具
│       └── KResult.kt               # 统一响应结果封装
│
├── kairowan-core/                   # 核心框架模块
│   └── src/main/kotlin/com/kairowan/core/
│       ├── framework/               # 框架核心
│       │   ├── security/            # 安全认证 (LoginUser, PermissionUtils)
│       │   └── cache/               # 缓存抽象 (CacheProvider, RedisCacheProvider)
│       ├── cache/                   # 两级缓存实现 (TwoLevelCacheProvider)
│       ├── controller/              # 基础控制器 (KController, CommonController)
│       ├── service/                 # 基础服务 (KService)
│       ├── page/                    # 分页支持 (KPageRequest, KTableData)
│       ├── plugin/                  # Ktor 插件 (RequestLogPlugin, PerformanceMonitorPlugin)
│       ├── annotation/              # 注解定义 (@Log)
│       └── extensions/              # 扩展函数 (EntityExtensions)
│
├── kairowan-system/                 # 系统管理模块
│   └── src/main/kotlin/com/kairowan/system/
│       ├── controller/              # 系统 API
│       │   ├── AuthController.kt    # 认证 (登录/登出)
│       │   ├── CaptchaController.kt # 验证码
│       │   ├── SysUserController.kt # 用户管理
│       │   ├── SysRoleController.kt # 角色管理
│       │   ├── SysMenuController.kt # 菜单管理
│       │   ├── SysDeptController.kt # 部门管理
│       │   ├── SysPostController.kt # 岗位管理
│       │   ├── SysConfigController.kt # 系统配置
│       │   ├── SysDictController.kt # 数据字典
│       │   ├── ProfileController.kt # 个人中心
│       │   ├── FileController.kt    # 文件管理
│       │   └── NotificationController.kt # 通知管理
│       ├── service/                 # 系统服务
│       │   ├── TokenService.kt      # Token 管理
│       │   ├── CaptchaService.kt    # 验证码服务
│       │   ├── SysLoginService.kt   # 登录服务
│       │   ├── SysPermissionService.kt # 权限服务
│       │   ├── SysUserService.kt    # 用户服务
│       │   ├── SysMenuService.kt    # 菜单服务
│       │   ├── SysRoleService.kt    # 角色服务
│       │   ├── SysDictService.kt    # 字典服务
│       │   ├── SysConfigService.kt  # 配置服务
│       │   ├── SysDeptService.kt    # 部门服务
│       │   ├── ProfileService.kt    # 个人中心服务
│       │   ├── FileService.kt       # 文件服务
│       │   ├── FileSyncService.kt   # 文件同步服务
│       │   └── CacheWarmupService.kt # 缓存预热服务
│       ├── domain/                  # 实体类
│       │   ├── SysUser.kt, SysRole.kt, SysMenu.kt
│       │   ├── SysOrg.kt, SysConfig.kt, SysDict.kt
│       │   ├── SysRoleMenu.kt, SysUserRole.kt
│       │   ├── SysFile.kt, SysNotification.kt
│       │   └── ...
│       └── vo/                      # 视图对象
│           ├── UserInfo.kt, UserInfoResult.kt
│           ├── LoginResult.kt, SysRoleVo.kt
│           └── ...
│
├── kairowan-monitor/                # 监控模块
│   └── src/main/kotlin/com/kairowan/monitor/
│       ├── controller/              # 监控 API
│       │   ├── MonitorController.kt # 监控管理
│       │   ├── DashboardController.kt # 仪表盘
│       │   ├── CacheMonitorController.kt # 缓存监控
│       │   └── AnalysisController.kt # 数据分析
│       ├── service/                 # 监控服务
│       │   ├── OnlineUserService.kt # 在线用户管理
│       │   ├── ServerMonitorService.kt # 服务器监控
│       │   ├── SysJobService.kt     # 定时任务管理
│       │   ├── SysLogService.kt     # 日志服务
│       │   ├── DashboardService.kt  # 仪表盘服务
│       │   └── AnalysisService.kt   # 数据分析服务
│       └── domain/                  # 实体类
│           ├── SysJob.kt, SysLoginLog.kt
│           └── SysOperLog.kt
│
├── kairowan-generator/              # 代码生成器模块
│   └── src/main/kotlin/com/kairowan/generator/
│       ├── controller/              # 生成器 API
│       │   └── GenController.kt
│       ├── core/                    # 生成器核心
│       │   ├── CodeGenerator.kt     # 代码生成器
│       │   └── TableMetadataReader.kt # 表元数据读取
│       └── service/                 # 生成器服务
│           └── GeneratorService.kt
│
└── kairowan-app/                    # 应用启动模块
    ├── build.gradle.kts             # 应用构建配置
    └── src/main/
        ├── kotlin/com/kairowan/app/
        │   ├── Application.kt       # 主入口
        │   └── AppModules.kt        # Koin 依赖注入配置
        └── resources/
            ├── application.conf     # 配置文件
            ├── logback.xml          # 日志配置
            ├── db/migration/        # Flyway 数据库迁移脚本 (7个版本)
            └── sql/                 # SQL 初始化脚本
```

---

## 🔧 配置说明

### 环境变量覆盖
所有配置都支持环境变量覆盖：

```bash
# 数据库配置
export DB_URL="jdbc:mysql://prod-db:3306/kairowan"
export DB_USER="prod_user"
export DB_PASSWORD="secure_password"
export DB_WARMUP_ENABLED=true
export FLYWAY_ENABLED=false
export FLYWAY_RUN_ONCE=true

# Redis 配置
export REDIS_HOST="prod-redis"
export REDIS_PORT=6379
export REDIS_PASSWORD="redis_password"

# JWT 配置 (生产环境必须覆盖)
export JWT_SECRET="your-very-long-and-secure-secret-key-at-least-32-chars"

# 文件配置
export FILE_UPLOAD_PATH="/data/uploads"
export FILE_URL_PREFIX="https://cdn.example.com/files"
export FILE_SYNC_ON_STARTUP=true
export FILE_SYNC_ONCE=true

# 监控配置
export METRICS_ENABLED=true
export REQUEST_LOG_ENABLED=true

# 启动应用
./gradlew :kairowan-app:run
```

### 性能优化配置

#### 数据库优化
```hocon
db {
    # Flyway 配置
    flyway {
        enabled = false      # 生产环境禁用，加快启动
        runOnce = true       # 只在首次启动时迁移
        force = false        # 强制执行迁移
    }

    # 数据库预热
    warmup.enabled = false   # 禁用预热，加快启动 (首次请求会慢约1秒)

    # HikariCP 连接池
    hikari {
        maximumPoolSize = 20
        minimumIdle = 1      # 降低启动时连接数
        connectionTimeout = 10000
    }
}
```

#### 文件同步优化
```hocon
file {
    syncOnStartup = true     # 启动时同步文件
    syncOnce = true          # 仅首次同步 (通过标记文件控制)
    syncMarker = ".kairowan_file_sync.done"  # 同步标记文件
}
```

### MinIO 配置 (可选)
```hocon
minio {
    endpoint = "http://localhost:9000"
    endpoint = ${?MINIO_ENDPOINT}
    accessKey = "minioadmin"
    accessKey = ${?MINIO_ACCESS_KEY}
    secretKey = "minioadmin"
    secretKey = ${?MINIO_SECRET_KEY}
}
```

---

## 🧪 测试

```bash
# 运行所有测试
./gradlew test

# 运行特定模块测试
./gradlew :kairowan-system:test
./gradlew :kairowan-core:test

# 生成测试覆盖率报告
./gradlew test jacocoTestReport

# 查看报告
open build/reports/tests/test/index.html
```

---

## 📦 模块开发

### 编译特定模块
```bash
# 只编译 common 模块
./gradlew :kairowan-common:build

# 只编译 system 模块
./gradlew :kairowan-system:build

# 并行编译所有模块
./gradlew build --parallel
```

### 添加新模块
```bash
# 1. 创建模块目录
mkdir -p kairowan-newmodule/src/{main,test}/{kotlin,resources}

# 2. 创建 build.gradle.kts
cat > kairowan-newmodule/build.gradle.kts << 'EOF'
plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":kairowan-core"))
}
EOF

# 3. 在 settings.gradle.kts 中注册
echo 'include(":kairowan-newmodule")' >> settings.gradle.kts

# 4. 刷新 Gradle
./gradlew --refresh-dependencies
```

---

## 🚀 部署

### Docker Compose 部署 (推荐)

**一键启动所有服务（MySQL + Redis + 应用）：**

```bash
# 1. 复制环境变量配置
cp .env.example .env

# 2. 编辑 .env 文件，修改密码
vim .env

# 3. 启动所有服务
docker-compose up -d

# 4. 查看日志
docker-compose logs -f kairowan-ktor

# 5. 停止所有服务
docker-compose down
```

### Docker 单独部署

```bash
# 1. 构建镜像
docker build -t kairowan-ktor:latest .

# 2. 运行容器
docker run -d \
  -p 8080:8080 \
  -e DB_URL="jdbc:mysql://your-db-host:3306/kairowan_ktor?..." \
  -e DB_USER="root" \
  -e DB_PASSWORD="your_password" \
  -e REDIS_HOST="your-redis-host" \
  -e REDIS_PASSWORD="your_redis_password" \
  -e JWT_SECRET="your-secret-key" \
  --name kairowan-ktor \
  kairowan-ktor:latest

# 3. 查看日志
docker logs -f kairowan-ktor

# 4. 停止容器
docker stop kairowan-ktor
```
  -e REDIS_HOST="your-redis-host" \
  -e REDIS_PASSWORD="your_redis_password" \
  -e JWT_SECRET="your-secret-key" \
  --name kairowan-ktor \
  kairowan-ktor:latest

# 3. 查看日志
docker logs -f kairowan-ktor

# 4. 停止容器
docker stop kairowan-ktor
```

### JAR 部署
```bash
# 1. 构建 JAR
./gradlew :kairowan-app:shadowJar

# 2. 运行（使用环境变量）
export DB_URL="jdbc:mysql://localhost:3306/kairowan_ktor?..."
export DB_USER="root"
export DB_PASSWORD="your_password"
export REDIS_HOST="localhost"
export REDIS_PASSWORD="your_redis_password"
export JWT_SECRET="your-secret-key"

java -jar kairowan-app/build/libs/kairowan-app-all.jar

# 3. 或使用配置文件
java -jar kairowan-app/build/libs/kairowan-app-all.jar
```

---

## 📊 性能指标

### 启动性能
- **优化前**: ~18 秒 (Flyway 17.3s + Database Warmup 18s)
- **优化后**: ~0.5 秒 (禁用 Flyway + 禁用 Warmup)
- **推荐配置**: ~1 秒 (禁用 Flyway + 启用 Warmup)

### 缓存性能
- **L1 缓存 (Caffeine)**: 5000 个 key，5 分钟过期，命中率 >90%
- **L2 缓存 (Redis)**: 分布式缓存，支持集群

### 数据库性能
- **连接池**: HikariCP，最大 20 个连接，最小 1 个空闲连接
- **连接优化**: 预编译语句缓存、批量操作重写、缓存服务器配置

---

## 📚 文档

- 📖 [快速开始指南](./QUICKSTART.md) - 详细的安装和运行指南
- 📋 [重构报告](./REFACTORING_REPORT.md) - 多模块化重构详细说明
- 🔧 [API 文档](http://localhost:8080/swagger-ui/index.html) - Swagger UI (需启动应用)

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request!

---

## 📄 License

[MIT License](LICENSE)

---

## 📞 联系方式

- 作者: Kairowan
- 邮箱: kairowan@example.com
- GitHub: https://github.com/kairowan/kairowan-ktor

---

## 🙏 致谢

感谢以下开源项目:
- [Ktor](https://ktor.io/) - 异步 Web 框架
- [Ktorm](https://www.ktorm.org/) - Kotlin ORM 框架
- [Koin](https://insert-koin.io/) - 依赖注入框架
- [HikariCP](https://github.com/brettwooldridge/HikariCP) - 高性能连接池
- [Caffeine](https://github.com/ben-manes/caffeine) - 高性能本地缓存
