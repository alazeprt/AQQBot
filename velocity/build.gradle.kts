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
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation("com.github.alazeprt:AConfiguration:1.2")
    implementation("com.alessiodp.libby:libby-velocity:2.0.0-SNAPSHOT")
    implementation("org.bstats:bstats-velocity:3.0.2")
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    compileOnly("me.lucko:spark-api:0.1-SNAPSHOT")
    compileOnly(fileTree("libs"))
}

tasks.jar {
    archiveFileName.set("AQQBot-${archiveFileName.get()}")
}

tasks.shadowJar {
    archiveFileName.set("AQQBot-${archiveFileName.get()}")
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

tasks.shadowJar {
    relocate("org.bstats", "top.alazeprt.aqqbot.lib.bstats")
}