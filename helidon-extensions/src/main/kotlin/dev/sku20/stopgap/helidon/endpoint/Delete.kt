package dev.sku20.stopgap.helidon.endpoint

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Delete(
    val path: String
)
