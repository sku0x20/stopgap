@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.jmh)
    application
}

group = "dev.sku20.stopgap.app"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(libs.bundles.helidon)
    implementation(project(":ir"))
    ksp(project(":ir"))
    implementation(project(":helidon-extensions"))
    ksp(project(":helidon-extensions"))

    implementation(libs.fastjson2.kotlin)

    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.kotlin)

    testImplementation(libs.helidon.webclient)
    testImplementation(libs.testcontainers)
}

application {
    mainClass = "dev.sku20.stopgap.app.MainKt"
}

ksp {
    arg("endpoint.codegen.registry.skip", "false")
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
        implementation(libs.junit.platform.launcher)
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
        implementation(libs.junit.platform.launcher)
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
