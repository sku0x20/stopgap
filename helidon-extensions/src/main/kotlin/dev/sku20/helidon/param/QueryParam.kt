package dev.sku20.helidon.param

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class QueryParam(
    val name: String
)
