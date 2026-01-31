object Versions {
    const val kotlin = "1.9.23"
    const val ktor = "2.3.9"
    const val ktorm = "3.6.0"
    const val koin = "3.5.3"
    const val logback = "1.4.14"
    const val mysql = "8.3.0"
    const val hikari = "5.1.0"
    const val jedis = "5.1.0"
    const val bcrypt = "0.4"
    const val poi = "5.2.5"
    const val quartz = "2.3.2"
    const val minio = "8.5.7"
    const val micrometer = "1.12.3"
    const val commonsLang3 = "3.14.0"
    const val swaggerUI = "2.9.0"
    const val caffeine = "3.1.8"
    const val flyway = "10.4.1"

    // Test
    const val kotest = "5.8.0"
    const val mockk = "1.13.8"
    const val coroutinesTest = "1.7.3"
}

object Libs {
    // Ktor Server
    const val ktorServerCore = "io.ktor:ktor-server-core-jvm"
    const val ktorServerNetty = "io.ktor:ktor-server-netty-jvm"
    const val ktorServerAuth = "io.ktor:ktor-server-auth-jvm"
    const val ktorServerAuthJwt = "io.ktor:ktor-server-auth-jwt-jvm"
    const val ktorServerContentNegotiation = "io.ktor:ktor-server-content-negotiation-jvm"
    const val ktorServerStatusPages = "io.ktor:ktor-server-status-pages-jvm"
    const val ktorServerCallLogging = "io.ktor:ktor-server-call-logging-jvm"
    const val ktorServerRateLimit = "io.ktor:ktor-server-rate-limit-jvm"
    const val ktorServerWebsockets = "io.ktor:ktor-server-websockets"
    const val ktorServerMetrics = "io.ktor:ktor-server-metrics-micrometer-jvm"
    const val ktorServerValidation = "io.ktor:ktor-server-request-validation"
    const val ktorSerializationJackson = "io.ktor:ktor-serialization-jackson-jvm"
    const val ktorServerDoubleReceive = "io.ktor:ktor-server-double-receive-jvm"
    const val ktorServerCors = "io.ktor:ktor-server-cors-jvm"

    // Ktor Client
    const val ktorClientCore = "io.ktor:ktor-client-core"
    const val ktorClientCio = "io.ktor:ktor-client-cio"
    const val ktorClientContentNegotiation = "io.ktor:ktor-client-content-negotiation"

    // Database
    const val ktormCore = "org.ktorm:ktorm-core:${Versions.ktorm}"
    const val ktormMysql = "org.ktorm:ktorm-support-mysql:${Versions.ktorm}"
    const val mysqlConnector = "com.mysql:mysql-connector-j:${Versions.mysql}"
    const val hikariCP = "com.zaxxer:HikariCP:${Versions.hikari}"
    const val flyway = "org.flywaydb:flyway-core:${Versions.flyway}"
    const val flywayMysql = "org.flywaydb:flyway-mysql:${Versions.flyway}"

    // DI
    const val koinKtor = "io.insert-koin:koin-ktor:${Versions.koin}"
    const val koinLogger = "io.insert-koin:koin-logger-slf4j:${Versions.koin}"

    // Cache
    const val jedis = "redis.clients:jedis:${Versions.jedis}"
    const val caffeine = "com.github.ben-manes.caffeine:caffeine:${Versions.caffeine}"

    // Security
    const val bcrypt = "org.mindrot:jbcrypt:${Versions.bcrypt}"

    // Utils
    const val commonsLang3 = "org.apache.commons:commons-lang3:${Versions.commonsLang3}"

    // Logging
    const val logback = "ch.qos.logback:logback-classic:${Versions.logback}"

    // Business
    const val poi = "org.apache.poi:poi-ooxml:${Versions.poi}"
    const val quartz = "org.quartz-scheduler:quartz:${Versions.quartz}"
    const val minio = "io.minio:minio:${Versions.minio}"
    const val prometheus = "io.micrometer:micrometer-registry-prometheus:${Versions.micrometer}"

    // Swagger
    const val swaggerUI = "io.github.smiley4:ktor-swagger-ui:${Versions.swaggerUI}"

    // Test
    const val kotestRunnerJunit5 = "io.kotest:kotest-runner-junit5:${Versions.kotest}"
    const val kotestAssertionsCore = "io.kotest:kotest-assertions-core:${Versions.kotest}"
    const val kotestProperty = "io.kotest:kotest-property:${Versions.kotest}"
    const val mockk = "io.mockk:mockk:${Versions.mockk}"
    const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutinesTest}"
    const val ktorServerTestHost = "io.ktor:ktor-server-test-host"
}
