pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.google.protobuf") version "0.9.4" apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // Ensure settings repositories are used
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ProtobufApp"
include(":app")
