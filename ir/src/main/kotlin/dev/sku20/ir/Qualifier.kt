package dev.sku20.ir

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Qualifier(
    val value: String
)
