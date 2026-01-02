package dev.sku20.helidon

import kotlin.reflect.KClass

interface Serde {
    fun <T : Any> serialize(obj: T): String
    fun <T : Any> deserialize(str: String, kClazz: KClass<T>): T
}