import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    implementation(project(":fabric:1.20.4"))!!
}

sourceSets {
    main {
        java {
            srcDir(project(":fabric:1.20.4").sourceSets.main.get().java)
        }
        kotlin {
            srcDir(project(":fabric:1.20.4").sourceSets.main.get().kotlin)
        }
        resources {
            srcDir(project(":fabric:1.20.4").sourceSets.main.get().resources)
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}