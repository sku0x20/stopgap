import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

val libs = the<LibrariesForLibs>()

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

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

dependencies {
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.kotlin)
}