plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
}

group = "dev.sku20.stopgap"
version = findProperty("publishVersion") as? String ?: "rolling"

dependencies {
    testImplementation(libs.assertj.core)
}

mavenPublishing {
    coordinates("dev.sku20.stopgap", "codegen", version as String)
    pom {
        name.set("Stopgap Codegen")
        description.set("Minimal Kotlin AST and printer for source generation.")
    }
}
