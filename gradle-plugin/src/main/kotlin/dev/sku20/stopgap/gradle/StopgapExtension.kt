package dev.sku20.stopgap.gradle

import org.gradle.api.provider.Property

interface StopgapExtension {
    val imageName: Property<String>
    val imageTag: Property<String>
    val e2eImageName: Property<String>
    val e2eImageTag: Property<String>
    val jarName: Property<String>
}
