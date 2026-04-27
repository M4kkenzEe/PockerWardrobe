import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    android {
        namespace = "com.ownstd.project.core.compose"
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
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.tooling.preview)

            implementation(libs.moko.resources)
            implementation(libs.moko.resources.compose)

            implementation(libs.flowmvi.core)
            implementation(libs.flowmvi.compose)

            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)

            implementation(project(":core:resources"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
