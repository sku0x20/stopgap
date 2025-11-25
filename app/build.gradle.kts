@file:Suppress("UnstableApiUsage")

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

repositories {
    mavenCentral()
}

group = "com.example.stopgap"
version = "1.0-SNAPSHOT"

val helidonVersion = "4.3.0"
val junitVersion = "6.0.1"

dependencies {
    implementation(libs.bundles.helidon)

    testImplementation(libs.assertj.core)
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.1.0")

    testImplementation(libs.helidon.webclient)

    testImplementation("org.testcontainers:testcontainers:2.0.2")
}

kotlin {
    jvmToolchain(25)
}

application {
    mainClass = "com.example.stopgap.MainKt"
}

tasks.register<Copy>("copyLibs") {
    from(configurations.runtimeClasspath)
    into(layout.buildDirectory.dir("libs/libs"))
}

tasks.jar {
    archiveFileName = "${rootProject.name}-${project.name}.jar"
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

tasks.register<Exec>("buildImage") {
    val context = rootProject.layout.projectDirectory.toString()
    val file = layout.projectDirectory.file("Dockerfile").toString()
    commandLine("docker", "build", "-t", "stopgap:latest", "-f", file, context)
}

tasks.register<Exec>("buildImageE2e") {
    val context = rootProject.layout.projectDirectory.toString()
    val file = layout.projectDirectory.file("Dockerfile").toString()
    commandLine("docker", "build", "-q", "-t", "stopgap:e2e", "-f", file, context)
}


// just for sharing code between e2eTest and intTest
testing.suites.register<JvmTestSuite>("sharedTest") {
    dependencies {
        // should not be needed but to extract out SharedStore
        // org.junit.platform.engine.support.store.Namespace is here
        implementation("org.junit.platform:junit-platform-launcher:${junitVersion}")
    }
    configurations {
        named("sharedTestImplementation").get().extendsFrom(testImplementation.get())
        named("sharedTestRuntimeOnly").get().extendsFrom(testRuntimeOnly.get())
    }
}

testing.suites.register<JvmTestSuite>("intTest") {
    sources {
        compileClasspath += sourceSets.main.get().output + sourceSets.named("sharedTest").get().output
        runtimeClasspath += sourceSets.main.get().output + sourceSets.named("sharedTest").get().output
    }
    configurations {
        named("intTestImplementation").get().extendsFrom(testImplementation.get())
        named("intTestRuntimeOnly").get().extendsFrom(testRuntimeOnly.get())
    }
}

testing.suites.register<JvmTestSuite>("e2eTest") {
    dependencies {
        implementation("org.junit.platform:junit-platform-launcher:${junitVersion}")
    }
    sources {
        compileClasspath += sourceSets.named("sharedTest").get().output
        runtimeClasspath += sourceSets.named("sharedTest").get().output
    }
    configurations {
        named("e2eTestImplementation").get().extendsFrom(testImplementation.get())
        named("e2eTestRuntimeOnly").get().extendsFrom(testRuntimeOnly.get())
    }
    targets.register("e2eTestImage") {
        testTask.configure {
            dependsOn("buildImageE2e")
            // todo: make it depend on output of task
            outputs.upToDateWhen { false }
        }
    }
}

testing.suites.configureEach {
    this as JvmTestSuite
    useJUnitJupiter(junitVersion)
    targets.configureEach {
        testTask.configure {
            testLogging {
                events(TestLogEvent.STANDARD_ERROR)
                exceptionFormat = TestExceptionFormat.FULL
            }
        }
    }
}
