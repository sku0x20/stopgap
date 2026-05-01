package dev.sku20.helidon.ksp

import kotlin.reflect.KClass

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class RegistryQualifier(
    val qualifier: String = "",
    val clazz: KClass<*> = Unit::class
)
