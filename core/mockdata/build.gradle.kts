import java.util.Properties
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
}

// Читаем .config/test_params.cfg из корня проекта
val testParamsCfg = rootProject.file(".config/test_params.cfg")
val useMockData: Boolean = if (testParamsCfg.exists()) {
    Properties().apply { load(testParamsCfg.inputStream()) }
        .getProperty("use_mock_data", "false").toBoolean()
} else false

kotlin {
    android {
        namespace = "com.ownstd.project.core.mockdata"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = 24

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            // Добавляем сгенерированный AppConfig.kt как source
            kotlin.srcDir("build/generated/appconfig/kotlin")
            dependencies {
                implementation(libs.koin.core)
                implementation(project(":features:wardrobe"))
                implementation(project(":features:outfit"))
                implementation(project(":features:profile"))
                implementation(project(":features:outfit_constructor"))
            }
        }
    }
}

// Генерируем AppConfig.kt из значения use_mock_data
val generateAppConfig by tasks.registering {
    val outDir = layout.buildDirectory.dir(
        "generated/appconfig/kotlin/com/ownstd/project/core/mockdata"
    )
    outputs.dir(outDir)
    doLast {
        outDir.get().asFile.mkdirs()
        outDir.get().file("AppConfig.kt").asFile.writeText(
            """
            package com.ownstd.project.core.mockdata

            object AppConfig {
                const val USE_MOCK_DATA = $useMockData
            }
            """.trimIndent()
        )
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    dependsOn(generateAppConfig)
}
