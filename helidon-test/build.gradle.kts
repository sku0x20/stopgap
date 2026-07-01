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
    api(libs.helidon.webserver)
    api(libs.helidon.webclient)
    api(libs.junit.jupiter.api)
    api(libs.junit.platform.launcher)
    implementation(libs.testcontainers)
}
