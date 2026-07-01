plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "dev.sku20.stopgap"
version = findProperty("publishVersion") as? String ?: "rolling"

dependencies {
    implementation(libs.helidon.webserver)
    implementation(libs.helidon.webclient)
    implementation(libs.junit.jupiter.api)
    implementation(libs.junit.platform.launcher)
}
