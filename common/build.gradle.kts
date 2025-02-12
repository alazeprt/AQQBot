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
    implementation("com.github.alazeprt:AConfiguration:1.0")
    implementation("com.github.alazeprt:taboolib-database:1.0.4")
    compileOnly("me.lucko:spark-api:0.1-SNAPSHOT")
}