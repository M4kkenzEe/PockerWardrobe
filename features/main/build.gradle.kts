import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        namespace = "com.ownstd.project.main"
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
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)

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
            implementation(project(":core:storage"))
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
