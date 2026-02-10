# 配置文件说明

## 快速开始

1. 复制配置模板文件：
```bash
cp kairowan-app/src/main/resources/application.yaml.example kairowan-app/src/main/resources/application.yaml
```

2. 编辑 `application.yaml`，修改以下敏感信息：

### 必须修改的配置项

#### 数据库配置
```yaml
db:
  url: "jdbc:mysql://YOUR_HOST:3306/kairowan_ktor?..."
  user: "YOUR_DB_USER"
  password: "YOUR_DB_PASSWORD"
```

#### Redis 配置
```yaml
redis:
  host: "YOUR_REDIS_HOST"
  port: 6379
  password: "YOUR_REDIS_PASSWORD"
```

#### JWT 密钥（生产环境必须修改）
```yaml
jwt:
  secret: "YOUR_VERY_LONG_AND_SECURE_SECRET_KEY_AT_LEAST_32_CHARS"
```

## 环境变量方式（推荐生产环境）

你也可以通过环境变量覆盖配置，无需修改配置文件：

```bash
# 数据库配置
export DB_URL="jdbc:mysql://your-host:3306/kairowan_ktor?..."
export DB_USER="your_user"
export DB_PASSWORD="your_password"

# Redis 配置
export REDIS_HOST="your-redis-host"
export REDIS_PORT=6379
export REDIS_PASSWORD="your_redis_password"

# JWT 配置
export JWT_SECRET="your-very-long-and-secure-secret-key"

# 启动应用
./gradlew :kairowan-app:run
```

## 支持的环境变量

### 数据库
- `DB_URL` - 数据库连接 URL
- `DB_USER` - 数据库用户名
- `DB_PASSWORD` - 数据库密码
- `DB_MAX_POOL_SIZE` - 最大连接数
- `DB_MIN_IDLE` - 最小空闲连接数
- `DB_WARMUP_ENABLED` - 是否启用数据库预热
- `FLYWAY_ENABLED` - 是否启用 Flyway 迁移
- `FLYWAY_RUN_ONCE` - 是否只在首次启动时迁移
- `FLYWAY_FORCE` - 是否强制执行迁移

### Redis
- `REDIS_HOST` - Redis 主机地址
- `REDIS_PORT` - Redis 端口
- `REDIS_PASSWORD` - Redis 密码
- `REDIS_MAX_TOTAL` - 最大连接数
- `REDIS_MAX_IDLE` - 最大空闲连接数
- `REDIS_MIN_IDLE` - 最小空闲连接数

### JWT
- `JWT_SECRET` - JWT 密钥（生产环境必须设置）

### 文件上传
- `FILE_UPLOAD_PATH` - 文件上传路径
- `FILE_URL_PREFIX` - 文件访问 URL 前缀
- `FILE_SYNC_ON_STARTUP` - 启动时是否同步文件
- `FILE_SYNC_ONCE` - 是否只同步一次

### 监控
- `METRICS_ENABLED` - 是否启用指标监控
- `METRICS_PATH` - 指标路径
- `REQUEST_LOG_ENABLED` - 是否启用请求日志

### CORS
- `CORS_ALLOWED_ORIGINS` - 允许的跨域来源

### 服务器
- `PORT` - 服务器端口

## Docker 部署示例

```bash
docker run -d \
  -p 8080:8080 \
  -e DB_URL="jdbc:mysql://db:3306/kairowan" \
  -e DB_USER="root" \
  -e DB_PASSWORD="password" \
  -e REDIS_HOST="redis" \
  -e REDIS_PASSWORD="redis_password" \
  -e JWT_SECRET="your-secret-key" \
  --name kairowan-ktor \
  kairowan-ktor:latest
```

## 安全建议

1. **永远不要提交包含真实密码的配置文件到 Git**
2. **生产环境必须修改默认的 JWT 密钥**
3. **使用强密码**：数据库和 Redis 密码应该足够复杂
4. **使用环境变量**：生产环境推荐使用环境变量而不是配置文件
5. **定期更换密钥**：定期更换 JWT 密钥和数据库密码
6. **限制访问**：确保数据库和 Redis 只能从应用服务器访问

## 配置文件优先级

1. 环境变量（最高优先级）
2. `application.yaml`
3. `application.yaml.example`（仅作为模板，不会被加载）
