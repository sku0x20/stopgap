package dev.sku20.helidon.endpoint

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Trace(
    val path: String
)
