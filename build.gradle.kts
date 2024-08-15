import io.izzel.taboolib.gradle.BUKKIT
import io.izzel.taboolib.gradle.UNIVERSAL
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("io.izzel.taboolib") version "2.0.11"
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
}

taboolib {
    env {
        // 安装模块
        install(UNIVERSAL, BUKKIT)
    }
    version { taboolib = "6.1.2-beta10" }
}

repositories {
    maven("https://maven.nova-committee.cn/releases")
    mavenCentral()
}

dependencies {
    taboo("cn.evole.onebot:OneBot-Client:0.4.0")
    compileOnly("ink.ptms.core:v12004:12004:mapped")
    compileOnly("ink.ptms.core:v12004:12004:universal")
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
