plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "dev.sku20"
version = "rolling"

dependencies {
    implementation(libs.ksp.api)
    testImplementation(libs.assertj.core)
}
