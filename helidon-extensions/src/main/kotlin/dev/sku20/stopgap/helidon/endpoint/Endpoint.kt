package dev.sku20.stopgap.helidon.endpoint

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Endpoint(
    val path: String
)
