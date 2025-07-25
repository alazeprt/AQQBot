import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.gradleup.shadow") version "8.3.0"
}

group = "top.alazeprt.aqqbot"
version = properties["version"] as String

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://repo.opencollab.dev/main/")
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    compileOnly("net.kyori:adventure-platform-bukkit:4.3.4")
    compileOnly("com.github.alazeprt:AConfiguration:1.2")
    implementation("com.alessiodp.libby:libby-bukkit:2.0.0-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("org.glavo:rcon-java:3.0")
    compileOnly("dev.folia:folia-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("me.lucko:spark-api:0.1-SNAPSHOT")
}

tasks.jar {
    archiveFileName.set("AQQBot-${archiveFileName.get()}")
}

tasks.shadowJar {
    archiveFileName.set("AQQBot-${archiveFileName.get()}")
    relocate("org.bstats", "top.alazeprt.aqqbot.lib.bstats")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}