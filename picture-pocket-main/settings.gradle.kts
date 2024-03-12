pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
        maven { url=uri("https://www.jitpack.io") }
    }
}

rootProject.name = "FinalProject"
include(":app")
include(":mylibrary")

 