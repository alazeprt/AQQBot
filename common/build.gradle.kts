import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

group = "top.alazeprt.aqqbot"
version = properties["version"] as String

repositories {
    maven("https://jitpack.io")
    maven("https://repo.lucko.me/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.opencollab.dev/main/")
    mavenCentral()
}

dependencies {
    implementation("com.alessiodp.libby:libby-core:2.0.0-SNAPSHOT")
    compileOnly("com.github.alazeprt:AOneBot:1.0.16-beta")
    compileOnly("com.google.code.gson:gson:2.11.0")
    compileOnly("net.kyori:adventure-api:4.18.0")
    compileOnly("net.kyori:adventure-text-serializer-legacy:4.24.0")
    compileOnly("com.github.alazeprt:AConfiguration:1.2")
    compileOnly("com.github.alazeprt:taboolib-database:1.0.4")
    compileOnly("me.lucko:spark-api:0.1-SNAPSHOT")
    compileOnly("org.java-websocket:Java-WebSocket:1.5.7")
    compileOnly("com.microsoft.playwright:playwright:1.53.0")
    compileOnly("org.openjdk.nashorn:nashorn-core:15.6")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}