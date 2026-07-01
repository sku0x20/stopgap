plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
}

group = "dev.sku20.stopgap"
version = findProperty("publishVersion") as? String ?: "rolling"

mavenPublishing {
    coordinates("dev.sku20.stopgap", "helidon-test", version as String)
    pom {
        name.set("Stopgap Helidon Test")
        description.set("JUnit extensions and test utilities for integration and e2e testing with Helidon.")
    }
}

dependencies {
    implementation(libs.helidon.webserver)
    implementation(libs.testcontainers)
    implementation(libs.helidon.webclient)
    implementation(libs.junit.jupiter.api)
    implementation(libs.junit.platform.launcher)
}
