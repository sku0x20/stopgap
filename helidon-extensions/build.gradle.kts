plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "dev.sku20.stopgap"
version = "rolling"

dependencies {
    implementation(libs.ksp.api)
    implementation(libs.helidon.webserver)

    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.kotlin)
}

configurations {
    val kspApi = libs.ksp.api.get()
    runtimeElements {
        exclude(group = kspApi.group, module = kspApi.name)
    }
}
