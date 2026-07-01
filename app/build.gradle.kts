plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.jmh)
    application
    id("stopgap")
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

    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.kotlin)

    testImplementation(project(":helidon-test"))
}

application {
    mainClass = "dev.sku20.stopgap.app.MainKt"
}

ksp {
    arg("endpoint.codegen.registry.skip", "false")
}

stopgap {
    imageName = "stopgap"
}
