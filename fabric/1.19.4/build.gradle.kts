dependencies {
    implementation(project(":fabric:1.19.2"))!!
}

sourceSets {
    main {
        java {
            srcDir(project(":fabric:1.19.2").sourceSets.main.get().java)
        }
        kotlin {
            srcDir(project(":fabric:1.19.2").sourceSets.main.get().kotlin)
        }
        resources {
            srcDir(project(":fabric:1.19.2").sourceSets.main.get().resources)
        }
    }
}