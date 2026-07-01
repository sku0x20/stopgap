package dev.sku20.stopgap.gradle

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

interface StopgapExtension {
    val imageName: Property<String>
    val imageTag: Property<String>
    val e2eImageName: Property<String>
    val e2eImageTag: Property<String>
    val jarName: Property<String>
    val dockerfile: RegularFileProperty
    val dockerContext: DirectoryProperty
}
