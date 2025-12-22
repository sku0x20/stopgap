@file:Suppress("UnstableApiUsage")

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "dev.sku20"
version = "rolling"

dependencies {
    implementation(libs.ksp.api)
    testImplementation(libs.assertj.core)
}

// -- COMMON CONFIGS --
kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())
}

testing.suites.configureEach {
    this as JvmTestSuite
    useJUnitJupiter(libs.versions.junit)
    targets.configureEach {
        testTask.configure {
            testLogging {
                events(TestLogEvent.STANDARD_ERROR)
                exceptionFormat = TestExceptionFormat.FULL
            }
        }
    }
}
// --