import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm") version "2.2.20"
    application
}

repositories {
    mavenCentral()
}

group = "com.example.stopgap"
version = "1.0-SNAPSHOT"

val helidonVersion = "4.2.7"
val junitVersion = "6.0.0-RC3"

dependencies {
    implementation("io.helidon.webserver:helidon-webserver:${helidonVersion}")
    implementation("io.helidon.config:helidon-config-yaml:${helidonVersion}")

    testImplementation("org.assertj:assertj-core:3.27.3")
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.0.0")

    testImplementation("io.helidon.webserver.testing.junit5:helidon-webserver-testing-junit5:${helidonVersion}")
}

kotlin {
    jvmToolchain(25)
}

application {
    mainClass = "com.example.stopgap.MainKt"
}

tasks.register<Copy>("copyLibs") {
    from(configurations.runtimeClasspath)
    into("build/libs/libs")
}

tasks.jar {
    archiveFileName = "${project.name}.jar"
    val configuration = configurations.runtimeClasspath.get()
    val files = configuration.joinToString(" ") { "libs/${it.name}" }
    manifest {
        attributes(
            "Main-Class" to application.mainClass.get(),
            "Class-Path" to files
        )
    }
    dependsOn(tasks.named("copyLibs"))
}

testing {
    suites {
        named<JvmTestSuite>("test") {
            useJUnitJupiter(junitVersion)
        }
        register<JvmTestSuite>("integrationTest") {
            useJUnitJupiter(junitVersion)
            sources {
                compileClasspath += sourceSets.main.get().output
                runtimeClasspath += sourceSets.main.get().output
            }
            configurations {
                named("integrationTestImplementation").get().extendsFrom(testImplementation.get())
                named("integrationTestRuntimeOnly").get().extendsFrom(testRuntimeOnly.get())
            }
        }
    }
}

testing.suites.configureEach {
    targets.configureEach {
        this as JvmTestSuiteTarget
        testTask.configure {
            testLogging {
                events(TestLogEvent.STANDARD_ERROR)
                exceptionFormat = TestExceptionFormat.FULL
            }
        }
    }
}
