plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.jmh)
    application
    id("dev.sku20.stopgap")
}

group = "dev.sku20.stopgap.app"
version = "1.0-SNAPSHOT"

// Own configurations, not runtimeOnly: runtimeClasspath ships in libs/ on the app classpath, and the agent must load
// only via -javaagent (RFC-0002). Extensions go to one directory; the agent loads every jar in it.
val otelAgent = configurations.create("otelAgent")
val otelAgentExtensions = configurations.create("otelAgentExtensions")

repositories {
    exclusiveContent {
        forRepository {
            ivy {
                url = uri("https://github.com/grafana/otel-profiling-java/releases/download")
                patternLayout { artifact("v[revision]/[artifact].[ext]") }
                metadataSources { artifact() }
            }
        }
        filter { includeModule("grafana", "pyroscope-otel-javaagent-extension") }
    }
}

dependencies {
    implementation(libs.bundles.helidon)
    implementation(libs.helidon.webclient)
    implementation(libs.helidon.webclient.tracing)
    implementation(libs.helidon.webclient.telemetry.metrics)
    implementation(project(":ir"))
    ksp(project(":ir"))
    implementation(project(":helidon-extensions"))
    ksp(project(":helidon-extensions"))

    implementation(libs.fastjson2.kotlin)
    implementation(libs.postgresql)
    implementation(libs.hikaricp)
    implementation(libs.jul.to.slf4j)
    implementation(libs.otel.instrumentation.annotations)
    implementation(libs.otel.instrumentation.jdbc)
    implementation(libs.otel.instrumentation.hikaricp)
    implementation(libs.otel.logback.appender)
    implementation(libs.otel.instrumentation.micrometer)
    implementation(libs.otel.instrumentation.runtime.telemetry)
    otelAgent(libs.otel.javaagent)
    otelAgentExtensions(libs.pyroscope.otel.extension)
    implementation(libs.logback.classic)
    runtimeOnly(libs.helidon.logging.slf4j)

    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.lincheck)

    testImplementation(project(":helidon-test"))
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.grafana)
    kspIntTest(project(":ir")) // for codegen inside intTests `ksp` only runs for main
}

application {
    mainClass = "dev.sku20.stopgap.app.MainKt"
}

tasks.register<Copy>("copyOtelAgent") {
    from(otelAgent) { rename { "opentelemetry-javaagent.jar" } }
    from(otelAgentExtensions) { into("extensions") }
    into(layout.buildDirectory.dir("otel"))
}

ksp {
    arg("stopgap.codegen.endpoint.registry.enabled", "true")
    arg("stopgap.codegen.endpoint.auth.defaultType", "public")
}

stopgap {
    imageName = "stopgap"
}
