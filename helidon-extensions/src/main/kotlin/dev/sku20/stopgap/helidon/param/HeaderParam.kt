package dev.sku20.stopgap.helidon.param

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class HeaderParam(
    val name: String
)
