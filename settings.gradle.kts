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

include(":composeApp")
include(":features:card-feature")
include(":features:pin-card-feature")
include(":features:recommendations-page-screen")
include(":features:wardrobe-screen")
include(":design-system")
include(":features:tiktok-feed")
include(":core:database")
include(":core:network")
include(":features:authorization")
include(":features:profile")
include(":core:storage")
