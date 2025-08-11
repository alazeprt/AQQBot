import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.incremental.createDirectory

plugins {
    kotlin("jvm") version "2.0.21"
    id("maven-publish")
    id("com.gradleup.shadow") version "8.3.0"
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(project(":velocity"))
    implementation(project(":bukkit"))
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

subprojects {
    tasks.withType<Jar> {
        manifest {
            attributes("Implementation-Version" to rootProject.version)
            attributes("Implementation-Vendor" to "alazeprt")
            attributes("Implementation-Website" to "https://aqqbot.alazeprt.top/")
        }
    }
}

tasks.register("package") {
    val outputDir = rootDir.resolve("outputs")
    outputDir.createDirectory()
    subprojects.forEach {
        if (it.project.name == "common" || it.project.name == "fabric") {
            return@forEach
        }

        if (it.tasks.map { it.name }.contains("shadowJar")) {
            dependsOn(it.tasks.named("shadowJar"))
            doLast {
                val file = it.tasks.getByName<AbstractArchiveTask>("shadowJar").archiveFile.get().asFile
                file.copyTo(outputDir.resolve(file.name), true)
            }
        } else if (it.tasks.map { it.name }.contains("remapJar")) {
            dependsOn(it.tasks.named("remapJar"))
            doLast {
                val file = it.tasks.getByName<AbstractArchiveTask>("remapJar").archiveFile.get().asFile
                file.copyTo(outputDir.resolve(file.name), true)
            }
        } else {
            dependsOn(it.tasks.named("jar"))
            doLast {
                val file = it.tasks.getByName<Jar>("jar").archiveFile.get().asFile
                file.copyTo(outputDir.resolve(file.name), true)
            }
        }
    }
}

tasks.clean {
    delete(rootDir.resolve("outputs"))
}