package com.example.stopgap.serde

import dev.sku20.helidon.Serde
import kotlin.reflect.KClass

class FastjsonSerde: Serde {

    override fun <T : Any> deserialize(bytes: ByteArray, kClazz: KClass<T>): T {
        TODO("Not yet implemented")
    }

    override fun <T : Any> serialize(obj: T): ByteArray {
        TODO("Not yet implemented")
    }
}