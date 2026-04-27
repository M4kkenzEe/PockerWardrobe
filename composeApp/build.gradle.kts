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
        namespace = "com.ownstd.project.composeapp"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            binaryOption("bundleId", "com.ownstd.project.composeapp")
        }
    }

    sourceSets {

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
        }
        commonMain.dependencies {
            implementation(project(":features:wardrobe"))
            implementation(project(":features:outfit"))
            implementation(project(":features:main"))
            implementation(project(":core:compose"))
            implementation(project(":core:deeplink"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(project(":core:mockdata"))

            //koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }
    }
}

// Workaround: moko-resources references removed/moved Kotlin compiler internal classes
// (KonanTarget.IOS_ARM32, KotlinLibraryLayoutImpl) when processing iOS test targets.
afterEvaluate {
    tasks.matching { it.name.startsWith("generateMR") && it.name.contains("Test") }.configureEach {
        enabled = false
    }
}
