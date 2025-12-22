@file:Suppress("UnstableApiUsage")

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
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

}