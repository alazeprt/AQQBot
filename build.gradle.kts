import io.izzel.taboolib.gradle.BUKKIT
import io.izzel.taboolib.gradle.Basic
import io.izzel.taboolib.gradle.Bukkit
import io.izzel.taboolib.gradle.UNIVERSAL
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("io.izzel.taboolib") version "2.0.17"
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
}

taboolib {
    env {
        // 安装模块
        install(Bukkit, Basic)
    }
    description {
        dependencies {
            name("spark").optional(true)
            name("PlayerStats").optional(true)
            name("PlaceholderAPI").optional(true)
        }
        contributors {
            name("alazeprt")
        }
    }

    version { taboolib = "6.2.0" }
}

repositories {
    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/releases/")
    mavenCentral()
}

dependencies {
    taboo("com.github.alazeprt:AOneBot:1.0.2-beta")
    compileOnly("me.lucko:spark-api:0.1-SNAPSHOT")
    compileOnly("io.github.ithotl:PlayerStats:2.0-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("ink.ptms.core:v12101:12101:mapped")
    compileOnly("ink.ptms.core:v12101:12101:universal")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
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
