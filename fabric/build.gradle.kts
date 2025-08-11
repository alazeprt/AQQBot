import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("fabric-loom") version "1.11-SNAPSHOT"
}

repositories {
    maven("https://repo.lucko.me/")
    maven("https://jitpack.io")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:1.16.5")
    mappings("net.fabricmc:yarn:1.16.5+build.10")
    modImplementation("net.fabricmc:fabric-loader:0.14.7")
}

subprojects {
    group = "top.alazeprt.aqqbot"
    version = properties["version"] as String

    apply(plugin = "fabric-loom")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    base {
        archivesBaseName = "AQQBot-fabric-${properties["minecraft_version"]}"
    }

    repositories {
        maven("https://repo.lucko.me/")
        maven("https://jitpack.io")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        mavenCentral()
    }

    dependencies {
        minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")
        mappings("net.fabricmc:yarn:${properties["yarn_mappings"]}")
        modImplementation("net.fabricmc:fabric-loader:0.14.7")
        modImplementation("net.fabricmc:fabric-language-kotlin:1.12.3+kotlin.2.0.21")
        modImplementation("net.fabricmc.fabric-api:fabric-api:${properties["fabric_version"]}")

        include(implementation(project(":common"))!!)
        include(implementation("net.kyori:adventure-platform-fabric:${properties["adventure_version"]}")!!)

        include(implementation("com.alessiodp.libby:libby-fabric:2.0.0-SNAPSHOT")!!)
        include(implementation("com.alessiodp.libby:libby-core:2.0.0-SNAPSHOT")!!)

        compileOnly("net.luckperms:api:5.4")
        compileOnly("me.lucko:spark-api:0.1-SNAPSHOT")
        compileOnly("com.github.alazeprt:AConfiguration:1.2")
    }

    tasks.processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(17)
    }

    java {
        withSourcesJar()

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.jar {
        inputs.property("archivesName", project.base.archivesName)

        from("LICENSE") {
            rename { "${it}_${inputs.properties["archivesName"]}" }
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = listOf("-Xjvm-default=all")
        }
    }
}