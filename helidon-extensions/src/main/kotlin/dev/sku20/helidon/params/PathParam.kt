package dev.sku20.helidon.params

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class PathParam(
    val name: String
)
