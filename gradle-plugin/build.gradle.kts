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
    signAllPublications()
    coordinates("dev.sku20.stopgap", "gradle-plugin", version as String)
    pom {
        name.set("Stopgap Gradle Plugin")
        description.set("Gradle plugin for Stopgap application projects.")
        inceptionYear.set("2026")
        url.set("https://github.com/sku0x20/stopgap/")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("sku0x20")
                name.set("sku20")
                url.set("https://github.com/sku0x20/")
            }
        }
        scm {
            url.set("https://github.com/sku0x20/stopgap/")
            connection.set("scm:git:git://github.com/sku0x20/stopgap.git")
            developerConnection.set("scm:git:ssh://git@github.com/sku0x20/stopgap.git")
        }
    }
}
