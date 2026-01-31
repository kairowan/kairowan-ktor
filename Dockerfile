# 多阶段构建 Dockerfile
# Stage 1: 构建阶段
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

# 复制 Gradle 配置文件
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle
COPY buildSrc ./buildSrc

# 复制所有模块的构建文件
COPY kairowan-common/build.gradle.kts ./kairowan-common/
COPY kairowan-core/build.gradle.kts ./kairowan-core/
COPY kairowan-system/build.gradle.kts ./kairowan-system/
COPY kairowan-monitor/build.gradle.kts ./kairowan-monitor/
COPY kairowan-generator/build.gradle.kts ./kairowan-generator/
COPY kairowan-app/build.gradle.kts ./kairowan-app/

# 下载依赖（利用 Docker 缓存）
RUN gradle dependencies --no-daemon || true

# 复制源代码
COPY kairowan-common/src ./kairowan-common/src
COPY kairowan-core/src ./kairowan-core/src
COPY kairowan-system/src ./kairowan-system/src
COPY kairowan-monitor/src ./kairowan-monitor/src
COPY kairowan-generator/src ./kairowan-generator/src
COPY kairowan-app/src ./kairowan-app/src

# 构建项目
RUN gradle :kairowan-app:shadowJar --no-daemon

# Stage 2: 运行阶段
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 安装必要的工具
RUN apk add --no-cache curl tzdata

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建非 root 用户
RUN addgroup -g 1000 kairowan && \
    adduser -D -u 1000 -G kairowan kairowan

# 从构建阶段复制 JAR 文件
COPY --from=builder /app/kairowan-app/build/libs/kairowan-app-all.jar /app/app.jar

# 创建必要的目录
RUN mkdir -p /app/uploads /app/logs && \
    chown -R kairowan:kairowan /app

# 切换到非 root 用户
USER kairowan

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# JVM 参数优化
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
