plugins {
    kotlin("jvm")
}

dependencies {
    // 依赖核心框架
    implementation(project(":kairowan-core"))
    implementation(project(":kairowan-common"))
    implementation(project(":kairowan-system"))

    // 数据库
    implementation(Libs.ktormCore)

    // Redis
    implementation(Libs.jedis)

    // 定时任务
    implementation(Libs.quartz)

    // Prometheus
    implementation(Libs.prometheus)

    // 测试
    testImplementation(Libs.kotestRunnerJunit5)
    testImplementation(Libs.kotestAssertionsCore)
    testImplementation(Libs.mockk)
}
