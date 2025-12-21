@file:Suppress("UnstableApiUsage")

rootProject.name = "stopgap"
include("app")

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