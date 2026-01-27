# Kairowan-Ktor 企业级脚手架

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-1.9.22-blue.svg" alt="Kotlin">
  <img src="https://img.shields.io/badge/Ktor-2.3.7-orange.svg" alt="Ktor">
## Module Scaffold

Generate a new module skeleton with the built-in scaffolder:

```bash
kotlin -classpath build/classes/kotlin/main com.kairowan.ktor.generator.ModuleScaffold inventory com.kairowan.ktor.modules src/main/kotlin
```

  <img src="https://img.shields.io/badge/License-MIT-green.svg" alt="License">
</p>

基于 **Kotlin + Ktor** 构建的企业级后台管理系统脚手架，开箱即用，功能完整。

---

## ✨ 功能特性

### 🔐 认证与权限
- [x] JWT Token 认证
- [x] 登录/登出 (Token 黑名单)
- [x] 图片验证码
- [x] RBAC 权限控制 (角色/菜单/权限)
- [x] 接口权限注解 (`requirePermission`)
- [x] 在线用户管理 (强制踢出)

### 📊 系统管理
- [x] 用户管理 CRUD
- [x] 角色管理 CRUD
- [x] 菜单管理 CRUD (动态路由)
- [x] 部门管理 (树形结构)
- [x] 岗位管理
- [x] 系统配置 (Redis 缓存)
- [x] 数据字典 (Redis 缓存)

### 📝 日志与监控
- [x] 操作日志自动记录 (Plugin)
- [x] 登录日志
- [x] 服务器监控 (CPU/内存/JVM/磁盘)

### 📁 文件管理
- [x] MinIO/OSS 集成
- [x] 文件上传记录
- [x] 预签名 URL 预览
- [x] 文件类型自动识别

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
- [x] 请求限流 (RateLimit)
- [x] 参数校验

### 🧰 开发工具
- [x] 代码生成器 (自动生成 Entity/Service/Controller)
- [x] Swagger UI 接口文档
- [x] Prometheus 指标监控

---

## 🏗️ 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin 1.9.22 |
| 框架 | Ktor 2.3.7 |
| 数据库 | MySQL 8.0 + Ktorm ORM |
| 缓存 | Redis (Jedis) |
| 连接池 | HikariCP |
| 认证 | JWT (Auth0) |
| 加密 | BCrypt + AES |
| 文件存储 | MinIO |
| 定时任务 | Quartz |
| DI | Koin |
| 文档 | Swagger UI |
| 监控 | Micrometer + Prometheus |

---

## 🚀 快速开始

### 环境要求
- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- MinIO (可选)

### 1. 克隆项目
```bash
git clone https://github.com/kairowan/kairowan-ktor.git
cd kairowan-ktor
```

### 2. 配置数据库
编辑 `src/main/resources/application.conf`:
```hocon
database {
    driver = "com.mysql.cj.jdbc.Driver"
    url = "jdbc:mysql://localhost:3306/kairowan_ktor"
    user = "root"
    password = "your_password"
}

redis {
    host = "localhost"
    port = 6379
    password = ""
}
```

### 3. 初始化数据库
```bash
mysql -u root -p kairowan_ktor < src/main/resources/sql/init.sql
```

### 4. 启动项目
```bash
./gradlew run
```

### 5. 访问
- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui/index.html
- Prometheus: http://localhost:8080/metrics

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
| 文件 | `/system/file/**` | 文件管理 |
| 日志 | `/monitor/operlog/**` | 操作日志 |
| 在线 | `/monitor/online/**` | 在线用户 |
| 监控 | `/monitor/server` | 服务器监控 |
| 任务 | `/monitor/job/**` | 定时任务 |
| 生成 | `/tool/gen/**` | 代码生成 |
| 幂等 | `/common/idempotent/**` | 幂等令牌 |
| WS | `/ws`, `/ws/user/{id}` | WebSocket |
| WS API | `/ws/api/**` | 消息推送 |

---

## 📁 项目结构

```
src/main/kotlin/com/kairowan/ktor/
├── common/                     # 公共模块
│   ├── constant/               # 常量定义
│   ├── exception/              # 异常处理
│   └── utils/                  # 工具类
├── framework/                  # 框架核心
│   ├── config/                 # 配置类
│   ├── file/                   # 文件存储
│   ├── koin/                   # DI 模块
│   ├── security/               # 安全认证
│   ├── task/                   # 定时任务
│   ├── websocket/              # WebSocket
│   └── web/
│       ├── annotation/         # 注解
│       ├── controller/         # 控制器
│       ├── domain/             # 实体
│       ├── dto/                # DTO
│       ├── page/               # 分页
│       ├── plugin/             # Ktor 插件
│       └── service/            # 服务层
├── generator/                  # 代码生成器
└── Application.kt              # 启动入口
```

---

## 🔧 配置说明

### MinIO 配置 (可选)
```hocon
minio {
    endpoint = "http://localhost:9000"
    accessKey = "minioadmin"
    secretKey = "minioadmin"
}
```

### JWT 配置
```hocon
jwt {
    secret = "your-secret-key"
    issuer = "kairowan"
    audience = "kairowan-users"
    realm = "kairowan-ktor"
}
```

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request!

---

## 📄 License

[MIT License](LICENSE)
