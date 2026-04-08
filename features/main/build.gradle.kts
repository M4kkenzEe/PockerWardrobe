import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization") version "2.2.0"
}

kotlin {
    tasks.create("testClasses")

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.navigation3.runtime)
            implementation(libs.navigation3.ui)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            implementation(libs.flowmvi.core)
            implementation(libs.flowmvi.compose)

            implementation(libs.moko.resources)
            implementation(libs.moko.resources.compose)

            implementation(project(":core:compose"))
            implementation(project(":core:resources"))
            implementation(project(":core:deeplink"))
            implementation(project(":features:authorization"))
            implementation(project(":features:wardrobe"))
            implementation(project(":features:outfit"))
            implementation(project(":features:outfit_constructor"))
            implementation(project(":features:profile"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.ownstd.project.main"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
