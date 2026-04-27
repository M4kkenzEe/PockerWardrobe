import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.mokoResources)
}

kotlin {
    android {
        namespace = "com.ownstd.project.core.resources"
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
            implementation(libs.compose.ui)

            implementation(libs.moko.resources)
            implementation(libs.moko.resources.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

multiplatformResources {
    resourcesPackage.set("com.ownstd.project.core.resources")
}

// Workaround: moko-resources references the removed KonanTarget.IOS_ARM32 (dropped in Kotlin 2.0)
// when generating resources for iOS test compilations. These tasks are never needed for building.
afterEvaluate {
    tasks.matching { it.name.startsWith("generateMR") && it.name.contains("Test") }.configureEach {
        enabled = false
    }
}
