plugins {
    kotlin("jvm")
}

group = "top.alazeprt.aqqbot"
version = "1.1.2"

repositories {
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    mavenCentral()
}

dependencies {
    implementation("com.github.alazeprt:AOneBot:1.0.8-beta")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("net.kyori:adventure-api:4.18.0")
    compileOnly("com.mysql:mysql-connector-j:8.4.0")
    compileOnly("org.xerial:sqlite-jdbc:3.46.1.0")
    compileOnly("me.lucko:spark-api:0.1-SNAPSHOT")
}