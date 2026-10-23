package dev.sku20.stopgap.gradle

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ValueSourceParameters

interface GitShaParameters : ValueSourceParameters {
    val dir: DirectoryProperty
}
