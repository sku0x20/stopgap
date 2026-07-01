plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "dev.sku20.stopgap"
version = findProperty("publishVersion") as? String ?: "rolling"
