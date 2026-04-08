rootProject.name = "KotlinProjectTesting"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":androidApp")
include(":core:database")
include(":core:network")
include(":core:storage")
include(":core:resources")
include(":core:compose")
include(":core:deeplink")
include(":features:authorization")
include(":features:wardrobe")
include(":features:outfit")
include(":features:outfit_constructor")
include(":features:profile")
include(":features:main")
