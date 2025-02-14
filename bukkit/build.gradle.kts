plugins {
    kotlin("jvm")
    id("com.gradleup.shadow") version "8.3.0"
}

group = "top.alazeprt.aqqbot"
version = "1.2.0"

repositories {
    maven("https://jitpack.io")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.extendedclip.com/releases/")
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation("net.kyori:adventure-platform-bukkit:4.3.4")
    implementation("com.github.alazeprt:AConfiguration:1.2")
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("me.lucko:spark-api:0.1-SNAPSHOT")
}