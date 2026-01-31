plugins {
    kotlin("jvm")
}

dependencies {
    // 基础库
    api(Libs.commonsLang3)
    api(Libs.logback)

    // Ktor (用于 HttpClient 和 ApplicationRequest)
    api(Libs.ktorServerCore)
    api(Libs.ktorClientCore)
    api(Libs.ktorClientCio)
    api(Libs.ktorClientContentNegotiation)
    api(Libs.ktorSerializationJackson)

    // 安全 (用于 BCrypt)
    api(Libs.bcrypt)

    // Excel (用于 POI)
    api(Libs.poi)

    // 测试
    testImplementation(Libs.kotestRunnerJunit5)
    testImplementation(Libs.kotestAssertionsCore)
    testImplementation(Libs.mockk)
}
