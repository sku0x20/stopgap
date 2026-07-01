plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
    alias(libs.plugins.maven.publish)
}

group = "dev.sku20.stopgap"
version = findProperty("publishVersion") as? String ?: "rolling"

gradlePlugin {
    plugins {
        create("stopgap") {
            id = "dev.sku20.stopgap"
            implementationClass = "dev.sku20.stopgap.gradle.StopgapPlugin"
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    coordinates("dev.sku20.stopgap", "gradle-plugin", version as String)
    pom {
        name.set("Stopgap Gradle Plugin")
        description.set("Gradle plugin for Stopgap application projects.")
    }
}
