#!/bin/bash

# Kairowan-Ktor 快速配置脚本
# 用于首次部署时快速生成配置文件

set -e

echo "🚀 Kairowan-Ktor 配置向导"
echo "=========================="
echo ""

CONFIG_FILE="kairowan-app/src/main/resources/application.conf"
EXAMPLE_FILE="kairowan-app/src/main/resources/application.conf.example"

# 检查配置文件是否已存在
if [ -f "$CONFIG_FILE" ]; then
    echo "⚠️  配置文件已存在: $CONFIG_FILE"
    read -p "是否覆盖? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ 取消配置"
        exit 1
    fi
fi

# 复制模板文件
echo "📋 复制配置模板..."
cp "$EXAMPLE_FILE" "$CONFIG_FILE"

# 收集配置信息
echo ""
echo "请输入以下配置信息（直接回车使用默认值）："
echo ""

# 数据库配置
read -p "数据库主机 [localhost]: " DB_HOST
DB_HOST=${DB_HOST:-localhost}

read -p "数据库端口 [3306]: " DB_PORT
DB_PORT=${DB_PORT:-3306}

read -p "数据库名称 [kairowan_ktor]: " DB_NAME
DB_NAME=${DB_NAME:-kairowan_ktor}

read -p "数据库用户名 [root]: " DB_USER
DB_USER=${DB_USER:-root}

read -sp "数据库密码: " DB_PASSWORD
echo ""

# Redis 配置
read -p "Redis 主机 [localhost]: " REDIS_HOST
REDIS_HOST=${REDIS_HOST:-localhost}

read -p "Redis 端口 [6379]: " REDIS_PORT
REDIS_PORT=${REDIS_PORT:-6379}

read -sp "Redis 密码（无密码直接回车）: " REDIS_PASSWORD
echo ""

# JWT 密钥
echo ""
echo "⚠️  JWT 密钥必须至少 32 个字符"
read -sp "JWT 密钥（留空自动生成）: " JWT_SECRET
echo ""

if [ -z "$JWT_SECRET" ]; then
    JWT_SECRET=$(openssl rand -base64 48 | tr -d '\n')
    echo "✅ 已自动生成 JWT 密钥"
fi

# 应用配置
echo ""
echo "📝 更新配置文件..."

# 构建数据库 URL
DB_URL="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=GMT%2B8&useServerPrepStmts=true&cachePrepStmts=true&prepStmtCacheSize=250&prepStmtCacheSqlLimit=2048&rewriteBatchedStatements=true&cacheResultSetMetadata=true&cacheServerConfiguration=true&elideSetAutoCommits=true&maintainTimeStats=false"

# 使用 sed 替换配置（macOS 兼容）
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    sed -i '' "s|url: \"jdbc:mysql://localhost:3306/kairowan_ktor.*\"|url: \"$DB_URL\"|g" "$CONFIG_FILE"
    sed -i '' "s|user: \"root\"|user: \"$DB_USER\"|g" "$CONFIG_FILE"
    sed -i '' "s|password: \"your_password_here\"|password: \"$DB_PASSWORD\"|g" "$CONFIG_FILE"
    sed -i '' "s|host: \"localhost\"|host: \"$REDIS_HOST\"|g" "$CONFIG_FILE"
    sed -i '' "s|port: 6379|port: $REDIS_PORT|g" "$CONFIG_FILE"
    sed -i '' "s|password: \"your_redis_password_here\"|password: \"$REDIS_PASSWORD\"|g" "$CONFIG_FILE"
    sed -i '' "s|secret: \"please-change-this-to-a-very-long-and-secure-secret-key-at-least-32-characters-long\"|secret: \"$JWT_SECRET\"|g" "$CONFIG_FILE"
else
    # Linux
    sed -i "s|url: \"jdbc:mysql://localhost:3306/kairowan_ktor.*\"|url: \"$DB_URL\"|g" "$CONFIG_FILE"
    sed -i "s|user: \"root\"|user: \"$DB_USER\"|g" "$CONFIG_FILE"
    sed -i "s|password: \"your_password_here\"|password: \"$DB_PASSWORD\"|g" "$CONFIG_FILE"
    sed -i "s|host: \"localhost\"|host: \"$REDIS_HOST\"|g" "$CONFIG_FILE"
    sed -i "s|port: 6379|port: $REDIS_PORT|g" "$CONFIG_FILE"
    sed -i "s|password: \"your_redis_password_here\"|password: \"$REDIS_PASSWORD\"|g" "$CONFIG_FILE"
    sed -i "s|secret: \"please-change-this-to-a-very-long-and-secure-secret-key-at-least-32-characters-long\"|secret: \"$JWT_SECRET\"|g" "$CONFIG_FILE"
fi

echo ""
echo "✅ 配置完成！"
echo ""
echo "📄 配置文件位置: $CONFIG_FILE"
echo ""
echo "⚠️  安全提示："
echo "   1. 请勿将 $CONFIG_FILE 提交到 Git"
echo "   2. 生产环境建议使用环境变量而不是配置文件"
echo "   3. 定期更换 JWT 密钥和数据库密码"
echo ""
echo "🚀 现在可以启动应用了："
echo "   ./gradlew :kairowan-app:run"
echo ""
