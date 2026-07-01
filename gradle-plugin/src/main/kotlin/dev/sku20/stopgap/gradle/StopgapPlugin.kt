package dev.sku20.stopgap.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class StopgapPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        StopgapPluginApply(project).apply()
    }
}
