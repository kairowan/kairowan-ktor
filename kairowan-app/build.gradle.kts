plugins {
    kotlin("jvm")
    id("io.ktor.plugin")
}

application {
    mainClass.set("com.kairowan.app.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf(
        "-Dio.ktor.development=\$isDevelopment",
        "-Djava.net.useSystemProxies=false",
        "-DsocksProxyHost=",
        "-DsocksProxyPort="
    )
}

dependencies {
    // 依赖所有业务模块
    implementation(project(":kairowan-common"))
    implementation(project(":kairowan-core"))
    implementation(project(":kairowan-system"))
    implementation(project(":kairowan-monitor"))
    implementation(project(":kairowan-generator"))

    // Ktor 运行时
    implementation(Libs.ktorServerNetty)
    implementation(Libs.ktorServerCallLogging)
    implementation(Libs.ktorServerRateLimit)
    implementation(Libs.ktorServerWebsockets)
    implementation(Libs.ktorServerMetrics)

    // Prometheus
    implementation(Libs.prometheus)

    // 数据库驱动
    runtimeOnly(Libs.mysqlConnector)

    // 业务库
    implementation(Libs.quartz)
    implementation(Libs.minio)

    // 测试
    testImplementation(Libs.kotestRunnerJunit5)
    testImplementation(Libs.kotestAssertionsCore)
    testImplementation(Libs.mockk)
    testImplementation(Libs.ktorServerTestHost)
}

ktor {
    docker {
        jreVersion.set(JavaVersion.VERSION_17)
        localImageName.set("kairowan-ktor")
        imageTag.set(project.version.toString())
    }
}
