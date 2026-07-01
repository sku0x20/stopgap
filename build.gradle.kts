@file:Suppress("UnstableApiUsage")

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.maven.publish) apply false
}

subprojects {

    layout.buildDirectory = rootProject.layout.buildDirectory.dir(project.name)

    repositories {
        mavenCentral()
    }

    apply(plugin = rootProject.libs.plugins.kotlin.jvm.get().pluginId)

    // kotlin {}
    configure<KotlinJvmExtension> {
        jvmToolchain(rootProject.libs.versions.jvm.get().toInt())
    }

    // testing {}
    configure<TestingExtension> {
        suites.configureEach {
            this as JvmTestSuite
            useJUnitJupiter(rootProject.libs.versions.junit)
            targets.configureEach {
                testTask.configure {
                    testLogging {
                        events(TestLogEvent.STANDARD_ERROR)
                        exceptionFormat = TestExceptionFormat.FULL
                    }
                }
            }
        }
    }

    pluginManager.withPlugin(rootProject.libs.plugins.maven.publish.get().pluginId) {
        configure<MavenPublishBaseExtension> {
            publishToMavenCentral()
            signAllPublications()
            pom {
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
    }

}