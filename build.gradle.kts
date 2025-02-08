import io.izzel.taboolib.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("io.izzel.taboolib") version "2.0.22"
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
}

taboolib {
    env {
        // 安装模块
        install(Bukkit, Basic, Velocity, Metrics, Database)
    }
    description {
        contributors {
            name("alazeprt")
        }
        dependencies {
            name("spark").optional(true).loadafter(true)
            name("PlaceholderAPI").with("bukkit").optional(true).loadafter(true)
        }
    }
    relocate("com.google.code.gson", "top.alazeprt.aqqbot.lib.gson")

    version { taboolib = "6.2.1-df22fb1" }
}

repositories {
    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/releases/")
    mavenCentral()
}

dependencies {
    taboo("com.github.alazeprt:AOneBot:1.0.8-beta")
    compileOnly("com.mysql:mysql-connector-j:8.4.0")
    compileOnly("org.xerial:sqlite-jdbc:3.46.1.0")
    compileOnly("me.lucko:spark-api:0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("ink.ptms.core:v12101:12101:mapped")
    compileOnly("ink.ptms.core:v12101:12101:universal")
    compileOnly("io.papermc:velocity:3.3.0:376")
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
