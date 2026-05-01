plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "dev.sku20"
version = "rolling"

dependencies {
    implementation(libs.ksp.api)
    implementation(libs.helidon.webserver)
}

configurations {
    val kspApi = libs.ksp.api.get()
    runtimeElements {
        exclude(group = kspApi.group, module = kspApi.name)
    }
}
