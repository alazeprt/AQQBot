import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

group = "top.alazeprt.aqqbot"
version = properties["version"] as String

repositories {
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    mavenCentral()
}

dependencies {
    implementation("com.alessiodp.libby:libby-core:2.0.0-SNAPSHOT")
    compileOnly("com.github.alazeprt:AOneBot:1.0.11-beta")
    compileOnly("com.google.code.gson:gson:2.11.0")
    compileOnly("net.kyori:adventure-api:4.18.0")
    compileOnly("com.github.alazeprt:AConfiguration:1.2")
    compileOnly("com.github.alazeprt:taboolib-database:1.0.4")
    compileOnly("me.lucko:spark-api:0.1-SNAPSHOT")
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