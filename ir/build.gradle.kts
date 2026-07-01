plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
}

group = "dev.sku20.stopgap"
version = "rolling"

dependencies {
    implementation(libs.ksp.api)
    testImplementation(libs.assertj.core)
}

configurations {
    val kspApi = libs.ksp.api.get()
    runtimeElements {
        exclude(group = kspApi.group, module = kspApi.name)
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    pom {
        name.set("Stopgap IR")
        description.set("Instance Registry - lightweight source-generated dependency injection.")
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
