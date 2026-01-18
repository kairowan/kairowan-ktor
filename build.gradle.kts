val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val koin_version: String by project
val ktorm_version: String by project

plugins {
    kotlin("jvm") version "1.9.23"
    id("io.ktor.plugin") version "2.3.9"
}

group = "com.kairowan.ktor"
version = "0.0.1-SNAPSHOT"

application {
    mainClass.set("com.kairowan.ktor.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("io.ktor:ktor-server-status-pages-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-server-double-receive-jvm")
    implementation("io.ktor:ktor-serialization-jackson-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    
    // Koin
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")

    // Ktorm & Database
    implementation("org.ktorm:ktorm-core:$ktorm_version")
    implementation("org.ktorm:ktorm-support-mysql:$ktorm_version")
    implementation("com.mysql:mysql-connector-j:8.3.0")
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Enterprise Enhancements
    // 1. Security (JWT & BCrypt)
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")
    implementation("org.mindrot:jbcrypt:0.4")
    
    // 2. Optimization (RateLimit)
    implementation("io.ktor:ktor-server-rate-limit-jvm")
    
    // 3. Cache (Redis - Jedis)
    implementation("redis.clients:jedis:5.1.0")
    
    // 4. Utils
    implementation("org.apache.commons:commons-lang3:3.14.0") // Useful string utils

    // 4.1 HTTP Client
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")

    // 5. DevEx (Swagger & Validation)
    implementation("io.ktor:ktor-server-request-validation:$ktor_version")
    implementation("io.github.smiley4:ktor-swagger-ui:2.9.0")

    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    // 6. Business Accelerators
    // Excel (POI)
    implementation("org.apache.poi:poi-ooxml:5.2.5")
    // Scheduling (Quartz)
    implementation("org.quartz-scheduler:quartz:2.3.2")
    
    // 7. Observability (Prometheus)
    implementation("io.ktor:ktor-server-metrics-micrometer-jvm")
    implementation("io.micrometer:micrometer-registry-prometheus:1.12.3")
    
    // 8. WebSocket
    implementation("io.ktor:ktor-server-websockets:$ktor_version")
    
    // 9. File Storage (MinIO)
    implementation("io.minio:minio:8.5.7")
}
