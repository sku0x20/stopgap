package dev.sku20.stopgap.gradle

import org.gradle.api.Project
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.testing.base.TestingExtension

@Suppress("UnstableApiUsage")
internal class StopgapPluginSetup(private val project: Project) {

    private val extension: StopgapExtension = project.extensions.create("stopgap", StopgapExtension::class.java).also {
        it.imageName.convention(project.name)
        it.imageTag.convention("latest")
        it.e2eImageName.convention(it.imageName)
        it.e2eImageTag.convention("e2e")
        it.jarName.convention(project.provider { "${project.rootProject.name}-${project.name}.jar" })
    }

    fun apply() {
        setupCopyLibs()
        setupJar()
        setupDockerTasks()
        setupTestSuites()
    }

    private fun setupCopyLibs() {
        project.tasks.register("copyLibs", Copy::class.java) {
            it.from(project.configurations.named("runtimeClasspath"))
            it.into(project.layout.buildDirectory.dir("libs/libs"))
        }
    }

    private fun setupJar() {
        val javaApplication = project.extensions.getByType(JavaApplication::class.java)
        val runtimeClasspath = project.configurations.named("runtimeClasspath")

        project.tasks.named("jar", Jar::class.java) { jar ->
            jar.archiveFileName.set(extension.jarName)
            jar.dependsOn(project.tasks.named("copyLibs"))
            jar.doFirst {
                val classPath = runtimeClasspath.get().joinToString(" ") { "libs/${it.name}" }
                jar.manifest.attributes(
                    mapOf(
                        "Main-Class" to javaApplication.mainClass.get(),
                        "Class-Path" to classPath,
                    )
                )
            }
        }
    }

    private fun setupDockerTasks() {
        val context = project.rootProject.layout.projectDirectory.asFile.absolutePath
        val dockerfile = project.layout.projectDirectory.file("Dockerfile").asFile.absolutePath

        project.tasks.register("buildImage", Exec::class.java) {
            it.commandLine(
                "docker", "build",
                "-t", "${extension.imageName.get()}:${extension.imageTag.get()}",
                "-f", dockerfile, context
            )
        }

        project.tasks.register("buildImageE2e", Exec::class.java) {
            it.commandLine(
                "docker", "build", "-q",
                "-t", "${extension.e2eImageName.get()}:${extension.e2eImageTag.get()}",
                "-f", dockerfile, context
            )
        }
    }

    private fun setupTestSuites() {
        val mainOutput = project.extensions.getByType(SourceSetContainer::class.java)
            .getByName("main").output
        val testing = project.extensions.getByType(TestingExtension::class.java)

        testing.suites.register("intTest", JvmTestSuite::class.java) { suite ->
            suite.sources.compileClasspath += mainOutput
            suite.sources.runtimeClasspath += mainOutput
            project.configurations.named("intTestImplementation") {
                it.extendsFrom(project.configurations.getByName("testImplementation"))
            }
            project.configurations.named("intTestRuntimeOnly") {
                it.extendsFrom(project.configurations.getByName("testRuntimeOnly"))
            }
        }

        testing.suites.register("e2eTest", JvmTestSuite::class.java) { suite ->
            project.configurations.named("e2eTestImplementation") {
                it.extendsFrom(project.configurations.getByName("testImplementation"))
            }
            project.configurations.named("e2eTestRuntimeOnly") {
                it.extendsFrom(project.configurations.getByName("testRuntimeOnly"))
            }
            suite.targets.register("e2eTestImage") { target ->
                target.testTask.configure {
                    it.dependsOn(project.tasks.named("buildImageE2e"))
                    it.outputs.upToDateWhen { false }
                }
            }
        }
    }
}
