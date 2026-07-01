package dev.sku20.stopgap.ir

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class Qualifier(
    val value: String
)
