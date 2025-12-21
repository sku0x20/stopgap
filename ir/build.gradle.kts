plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "dev.sku20"
version = "rolling"

// -- COMMON CONFIGS --
kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())
}
// --