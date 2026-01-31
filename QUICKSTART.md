# Kairowan-Ktor 快速开始指南

本指南将帮助你在 5 分钟内启动 Kairowan-Ktor 项目。

## 📋 前置要求

在开始之前，请确保你的系统已安装以下软件：

- ✅ **JDK 17+** - [下载地址](https://adoptium.net/)
- ✅ **MySQL 8.0+** - [下载地址](https://dev.mysql.com/downloads/mysql/)
- ✅ **Redis 6.0+** - [下载地址](https://redis.io/download)
- ✅ **Git** - [下载地址](https://git-scm.com/downloads)

### 验证安装

```bash
# 检查 Java 版本
java -version  # 应该显示 17 或更高版本

# 检查 MySQL
mysql --version

# 检查 Redis
redis-cli --version

# 检查 Git
git --version
```

---

## 🚀 快速开始（5 分钟）

### 步骤 1: 克隆项目

```bash
# 克隆后端项目
git clone https://github.com/kairowan/kairowan-ktor.git
cd kairowan-ktor

# (可选) 克隆前端项目
git clone https://github.com/kairowan/kairowan-admin.git
```

### 步骤 2: 创建数据库

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE IF NOT EXISTS kairowan_ktor CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 退出 MySQL
exit;

# 导入初始化数据
mysql -u root -p kairowan_ktor < kairowan-app/src/main/resources/sql/init_complete.sql
```

### 步骤 3: 配置项目

#### 方法 A: 使用自动配置脚本（推荐）

```bash
# 运行配置向导
./setup-config.sh
```

脚本会引导你输入以下信息：
- 数据库主机、端口、用户名、密码
- Redis 主机、端口、密码
- JWT 密钥（可自动生成）

#### 方法 B: 手动配置

```bash
# 1. 复制配置模板
cp kairowan-app/src/main/resources/application.conf.example \
   kairowan-app/src/main/resources/application.conf

# 2. 编辑配置文件
vim kairowan-app/src/main/resources/application.conf
```

**必须修改的配置项：**

```yaml
db:
  url: "jdbc:mysql://localhost:3306/kairowan_ktor?..."
  user: "root"
  password: "your_password"  # 修改为你的数据库密码

redis:
  host: "localhost"
  port: 6379
  password: "your_redis_password"  # 修改为你的 Redis 密码（如果有）

jwt:
  secret: "your-very-long-and-secure-secret-key"  # 生产环境必须修改
```

### 步骤 4: 启动 Redis

```bash
# macOS (使用 Homebrew)
brew services start redis

# Linux
sudo systemctl start redis

# 或直接运行
redis-server
```

### 步骤 5: 启动后端项目

```bash
# 方式 1: 使用 Gradle 运行（开发环境）
./gradlew :kairowan-app:run

# 方式 2: 构建并运行 JAR（生产环境）
./gradlew :kairowan-app:shadowJar
java -jar kairowan-app/build/libs/kairowan-app-all.jar
```

### 步骤 6: 验证启动

打开浏览器访问以下地址：

- **API 根路径**: http://localhost:8080
- **健康检查**: http://localhost:8080/health
- **Swagger 文档**: http://localhost:8080/swagger-ui/index.html
- **Prometheus 指标**: http://localhost:8080/metrics

如果看到以下响应，说明启动成功：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": "Welcome to Modular Architecture"
}
```

### 步骤 7: 登录系统

**默认管理员账号：**
- 用户名: `admin`
- 密码: `admin123`

**登录接口：**
```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "code": "1234",
    "uuid": "test-uuid"
  }'
```

---

## 🎨 启动前端项目（可选）

如果你需要使用前端管理界面：

```bash
# 进入前端项目目录
cd kairowan-admin

# 安装依赖
npm install
# 或使用 pnpm
pnpm install

# 启动开发服务器
npm run dev
# 或
pnpm dev
```

前端项目默认运行在 http://localhost:5173

---

## 🔧 常见问题

### 问题 1: 数据库连接失败

**错误信息**: `Communications link failure`

**解决方案**:
1. 检查 MySQL 是否正在运行
2. 检查数据库地址、端口、用户名、密码是否正确
3. 检查防火墙是否阻止了连接

```bash
# 检查 MySQL 状态
# macOS
brew services list | grep mysql

# Linux
sudo systemctl status mysql
```

### 问题 2: Redis 连接失败

**错误信息**: `Unable to connect to Redis`

**解决方案**:
1. 检查 Redis 是否正在运行
2. 检查 Redis 密码是否正确

```bash
# 测试 Redis 连接
redis-cli ping
# 应该返回 PONG

# 如果有密码
redis-cli -a your_password ping
```

### 问题 3: 端口被占用

**错误信息**: `Address already in use`

**解决方案**:
```bash
# 查找占用 8080 端口的进程
lsof -i :8080

# 杀死进程
kill -9 <PID>

# 或修改配置文件中的端口
# application.conf
ktor:
  deployment:
    port: 8081  # 改为其他端口
```

### 问题 4: Gradle 构建失败

**解决方案**:
```bash
# 清理构建缓存
./gradlew clean

# 刷新依赖
./gradlew --refresh-dependencies

# 重新构建
./gradlew build
```

### 问题 5: 找不到配置文件

**错误信息**: `application.conf not found`

**解决方案**:
```bash
# 确保已复制配置模板
cp kairowan-app/src/main/resources/application.conf.example \
   kairowan-app/src/main/resources/application.conf

# 检查文件是否存在
ls -la kairowan-app/src/main/resources/application.conf
```

---

## 🐳 Docker 快速启动（推荐）

如果你想使用 Docker 快速启动整个环境：

### 创建 docker-compose.yml

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: kairowan-mysql
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: kairowan_ktor
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      - ./kairowan-app/src/main/resources/sql/init_complete.sql:/docker-entrypoint-initdb.d/init.sql

  redis:
    image: redis:7-alpine
    container_name: kairowan-redis
    command: redis-server --requirepass password
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data

  kairowan-ktor:
    build: .
    container_name: kairowan-ktor
    environment:
      DB_URL: "jdbc:mysql://mysql:3306/kairowan_ktor?useUnicode=true&characterEncoding=utf8&useSSL=false"
      DB_USER: "root"
      DB_PASSWORD: "password"
      REDIS_HOST: "redis"
      REDIS_PASSWORD: "password"
      JWT_SECRET: "your-very-long-and-secure-secret-key-at-least-32-chars"
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis

volumes:
  mysql-data:
  redis-data:
```

### 启动所有服务

```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f kairowan-ktor

# 停止所有服务
docker-compose down
```

---

## 📚 下一步

现在你已经成功启动了项目，可以：

1. 📖 阅读 [API 文档](http://localhost:8080/swagger-ui/index.html)
2. 🔧 查看 [配置说明](kairowan-app/src/main/resources/CONFIG.md)
3. 🎨 启动 [前端项目](https://github.com/kairowan/kairowan-admin)
4. 📝 查看 [完整 README](README.md)
5. 🐛 遇到问题？查看 [常见问题](#常见问题) 或提交 [Issue](https://github.com/kairowan/kairowan-ktor/issues)

---

## 🔒 安全提示

⚠️ **重要**: 在生产环境部署前，请务必：

1. ✅ 修改默认的 JWT 密钥
2. ✅ 使用强密码（数据库、Redis）
3. ✅ 修改默认管理员密码
4. ✅ 启用 HTTPS
5. ✅ 配置防火墙规则
6. ✅ 定期更新依赖和密钥

---

## 💡 提示

- 开发环境建议禁用 Flyway (`flyway.enabled = false`) 以加快启动速度
- 生产环境建议使用环境变量而不是配置文件
- 使用 `./gradlew build --parallel` 可以并行编译，提升速度
- 查看 `logs/` 目录获取详细日志

---

## 📞 获取帮助

- 📧 邮箱: kairowan@example.com
- 🐛 问题反馈: [GitHub Issues](https://github.com/kairowan/kairowan-ktor/issues)
- 📖 文档: [项目 Wiki](https://github.com/kairowan/kairowan-ktor/wiki)

---

**祝你使用愉快！** 🎉
