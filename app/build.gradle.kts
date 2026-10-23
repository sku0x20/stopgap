plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.jmh)
    application
    id("dev.sku20.stopgap")
}

group = "dev.sku20.stopgap.app"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(libs.bundles.helidon)
    implementation(project(":ir"))
    ksp(project(":ir"))
    implementation(project(":helidon-extensions"))
    ksp(project(":helidon-extensions"))

    implementation(libs.fastjson2.kotlin)
    implementation(libs.postgresql)
    implementation(libs.hikaricp)
    implementation(libs.jul.to.slf4j)
    runtimeOnly(libs.logback.classic)
    runtimeOnly(libs.helidon.logging.slf4j)

    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.lincheck)

    testImplementation(project(":helidon-test"))
    testImplementation(libs.testcontainers.postgresql)
    kspIntTest(project(":ir")) // for codegen inside intTests `ksp` only runs for main
}

application {
    mainClass = "dev.sku20.stopgap.app.MainKt"
}

ksp {
    arg("stopgap.codegen.endpoint.registry.enabled", "true")
    arg("stopgap.codegen.endpoint.auth.defaultType", "public")
}

stopgap {
    imageName = "stopgap"
}
