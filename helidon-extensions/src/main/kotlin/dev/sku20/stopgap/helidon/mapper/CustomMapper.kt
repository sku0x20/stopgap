package dev.sku20.stopgap.helidon.mapper

import kotlin.reflect.KClass

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.SOURCE)
annotation class CustomMapper(
    val qualifier: String = "",
    val clazz: KClass<*> = Unit::class
)
