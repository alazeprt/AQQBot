pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/")
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "AQQBot"
include("common")
include("bukkit")
include("velocity")
include("folia")
include("fabric")
include("fabric:1.18.2")
findProject(":fabric:1.18.2")?.name = "1.18.2"
include("fabric:1.19.2")
findProject(":fabric:1.19.2")?.name = "1.19.2"
include("fabric:1.19.4")
findProject(":fabric:1.19.4")?.name = "1.19.4"
include("fabric:1.20.1")
findProject(":fabric:1.20.1")?.name = "1.20.1"
include("fabric:1.20.4")
findProject(":fabric:1.20.4")?.name = "1.20.4"
include("fabric:1.20.6")
findProject(":fabric:1.20.6")?.name = "1.20.6"
include("fabric:1.21.1")
findProject(":fabric:1.21.1")?.name = "1.21.1"
include("fabric:1.21.4")
findProject(":fabric:1.21.4")?.name = "1.21.4"
include("fabric:1.21.7")
findProject(":fabric:1.21.7")?.name = "1.21.7"
include("fabric:1.21.8")
findProject(":fabric:1.21.8")?.name = "1.21.8"
include("fabric:1.21.5")
findProject(":fabric:1.21.5")?.name = "1.21.5"
include("fabric:1.21")
findProject(":fabric:1.21")?.name = "1.21"
