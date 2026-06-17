pluginManagement {
    repositories {
        google()
        mavenCentral()
        // Gradle Plugin Portal is the canonical source for Kotlin plugin marker artifacts.
        gradlePluginPortal()
        maven(url = "https://plugins.gradle.org/m2/")
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "checking_kotlin"
include(":app")
