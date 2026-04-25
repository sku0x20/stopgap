package dev.sku20.helidon.serde

import kotlin.reflect.KClass
import kotlin.reflect.KType

class NopSerde : Serde {
    override fun <T : Any> deserialize(bytes: ByteArray, kClazz: KClass<T>): T {
        throw UnsupportedOperationException("NopSerde does not support deserialization")
    }

    override fun <T> deserialize(bytes: ByteArray, type: KType): T {
        throw UnsupportedOperationException("NopSerde does not support deserialization")
    }

    override fun <T> serialize(obj: T): ByteArray {
        throw UnsupportedOperationException("NopSerde does not support serialization")
    }
}