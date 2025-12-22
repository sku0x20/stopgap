plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "dev.sku20"
version = "rolling"

dependencies {
    compileOnly(libs.ksp.api)
    testImplementation(libs.assertj.core)
}
