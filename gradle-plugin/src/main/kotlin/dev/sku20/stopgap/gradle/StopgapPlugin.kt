package dev.sku20.stopgap.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.testing.base.TestingExtension

@Suppress("UnstableApiUsage")
class StopgapPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        setupCopyLibs(project)
        setupJar(project)
        setupDockerTasks(project)
        setupTestSuites(project)
    }

    private fun setupCopyLibs(project: Project) {
        project.tasks.register("copyLibs", Copy::class.java) {
            from(project.configurations.named("runtimeClasspath"))
            into(project.layout.buildDirectory.dir("libs/libs"))
        }
    }

    private fun setupJar(project: Project) {
        val javaApplication = project.extensions.getByType(JavaApplication::class.java)
        val runtimeClasspath = project.configurations.named("runtimeClasspath")

        project.tasks.named("jar", Jar::class.java) {
            archiveFileName.set("${project.rootProject.name}-${project.name}.jar")
            dependsOn(project.tasks.named("copyLibs"))
            doFirst {
                val classPath = runtimeClasspath.get().joinToString(" ") { "libs/${it.name}" }
                manifest.attributes(
                    "Main-Class" to javaApplication.mainClass.get(),
                    "Class-Path" to classPath,
                )
            }
        }
    }

    private fun setupDockerTasks(project: Project) {
        val context = project.rootProject.layout.projectDirectory.asFile.absolutePath
        val dockerfile = project.layout.projectDirectory.file("Dockerfile").asFile.absolutePath
        val imageName = project.name

        project.tasks.register("buildImage", Exec::class.java) {
            commandLine("docker", "build", "-t", "$imageName:latest", "-f", dockerfile, context)
        }

        project.tasks.register("buildImageE2e", Exec::class.java) {
            commandLine("docker", "build", "-q", "-t", "$imageName:e2e", "-f", dockerfile, context)
        }
    }

    private fun setupTestSuites(project: Project) {
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val mainOutput = sourceSets.getByName("main").output
        val testing = project.extensions.getByType(TestingExtension::class.java)

        testing.suites.register("intTest", JvmTestSuite::class.java) { suite ->
            suite.sources.compileClasspath += mainOutput
            suite.sources.runtimeClasspath += mainOutput
            project.configurations.named("intTestImplementation") {
                extendsFrom(project.configurations.getByName("testImplementation"))
            }
            project.configurations.named("intTestRuntimeOnly") {
                extendsFrom(project.configurations.getByName("testRuntimeOnly"))
            }
        }

        testing.suites.register("e2eTest", JvmTestSuite::class.java) { suite ->
            project.configurations.named("e2eTestImplementation") {
                extendsFrom(project.configurations.getByName("testImplementation"))
            }
            project.configurations.named("e2eTestRuntimeOnly") {
                extendsFrom(project.configurations.getByName("testRuntimeOnly"))
            }
            suite.targets.register("e2eTestImage") { target ->
                target.testTask.configure {
                    dependsOn(project.tasks.named("buildImageE2e"))
                    outputs.upToDateWhen { false }
                }
            }
        }
    }
}
