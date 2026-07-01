plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
}

group = "dev.sku20.stopgap"
version = findProperty("publishVersion") as? String ?: "rolling"

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

mavenPublishing {
    coordinates("dev.sku20.stopgap", "helidon-extensions", version as String)
    pom {
        name.set("Stopgap Helidon Extensions")
        description.set("Custom codegen and Helidon utilities for Stopgap.")
    }
}
