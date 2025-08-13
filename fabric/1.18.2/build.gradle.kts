dependencies {
    implementation(project(":fabric:1.16.5"))!!
}

sourceSets {
    main {
        java {
            srcDir(project(":fabric:1.16.5").sourceSets.main.get().java)
        }
        kotlin {
            srcDir(project(":fabric:1.16.5").sourceSets.main.get().kotlin)
        }
        resources {
            srcDir(project(":fabric:1.16.5").sourceSets.main.get().resources)
        }
    }
}