plugins {
    kotlin("jvm")
}

dependencies {
    // 依赖公共模块
    api(project(":kairowan-common"))

    // Ktor 核心
    api(Libs.ktorServerCore)
    api(Libs.ktorServerAuth)
    api(Libs.ktorServerAuthJwt)
    api(Libs.ktorServerContentNegotiation)
    api(Libs.ktorServerStatusPages)
    api(Libs.ktorServerValidation)
    api(Libs.ktorServerDoubleReceive)
    api(Libs.ktorSerializationJackson)
    api(Libs.ktorServerCors)

    // 数据库
    api(Libs.ktormCore)
    api(Libs.hikariCP)
    api(Libs.flyway)
    api(Libs.flywayMysql)

    // 缓存
    api(Libs.jedis)
    api(Libs.caffeine)

    // DI
    api(Libs.koinKtor)
    api(Libs.koinLogger)

    // 安全
    api(Libs.bcrypt)

    // HTTP Client
    api(Libs.ktorClientCore)
    api(Libs.ktorClientCio)
    api(Libs.ktorClientContentNegotiation)

    // 测试
    testImplementation(Libs.kotestRunnerJunit5)
    testImplementation(Libs.kotestAssertionsCore)
    testImplementation(Libs.mockk)
    testImplementation(Libs.coroutinesTest)
    testImplementation(Libs.ktorServerTestHost)
}
