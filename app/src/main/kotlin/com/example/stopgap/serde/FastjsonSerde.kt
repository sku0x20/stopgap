package com.example.stopgap.serde

import dev.sku20.helidon.serde.Serde
import kotlin.reflect.KClass

// todo: benchmark both Fastjson Serde implementations

// only supports non-generic types
// faster than generic one as avoid one if check.
class FastjsonSerde : Serde {

    override fun <T : Any> deserialize(bytes: ByteArray, kClazz: KClass<T>): T {
        TODO("Not yet implemented")
    }

    override fun <T : Any> serialize(obj: T): ByteArray {
        TODO("Not yet implemented")
    }
}