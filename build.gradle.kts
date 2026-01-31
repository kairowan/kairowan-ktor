plugins {
    kotlin("jvm") version "1.9.23" apply false
    id("io.ktor.plugin") version "2.3.9" apply false
}

allprojects {
    group = "com.kairowan.ktor"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    // 统一版本管理
    val ktorVersion = "2.3.9"

    dependencies {
        // 所有子模块共享的依赖
        "implementation"(kotlin("stdlib"))
        "testImplementation"(kotlin("test"))

        // 为所有子模块提供 Ktor BOM
        "implementation"(platform("io.ktor:ktor-bom:$ktorVersion"))
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
