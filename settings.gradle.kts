@file:Suppress("UnstableApiUsage")

rootProject.name = "stopgap"
include("app")
include("ir")

gradle.allprojects {
    if (project != project.rootProject) {
        project.layout.buildDirectory = project.rootProject.layout.buildDirectory.dir(project.name)
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}