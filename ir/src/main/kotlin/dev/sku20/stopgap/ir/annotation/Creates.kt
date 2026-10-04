package dev.sku20.stopgap.ir.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Creates(
    val eagerly: Boolean = false
)
