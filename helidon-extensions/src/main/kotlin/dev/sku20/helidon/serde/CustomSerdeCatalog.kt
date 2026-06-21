package dev.sku20.helidon.serde

import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class CustomSerdeCatalog(
    val qualifier: String = "",
    val clazz: KClass<*> = Unit::class
)