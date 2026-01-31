plugins {
    kotlin("jvm")
}

dependencies {
    // 依赖核心框架
    implementation(project(":kairowan-core"))

    // 数据库驱动
    implementation(Libs.mysqlConnector)
    implementation(Libs.ktormMysql)

    // Swagger
    implementation(Libs.swaggerUI)

    // Excel
    implementation(Libs.poi)

    // 测试
    testImplementation(Libs.kotestRunnerJunit5)
    testImplementation(Libs.kotestAssertionsCore)
    testImplementation(Libs.mockk)
    testImplementation(Libs.coroutinesTest)
}
