package com.example.stopgap.serde.fastjson

import dev.sku20.helidon.serde.Serde
import kotlin.reflect.KClass

class FastjsonSerde : Serde {

    override fun <T : Any> deserialize(bytes: ByteArray, kClazz: KClass<T>): T {
        TODO("Not yet implemented")
    }

    override fun <T : Any> serialize(obj: T): ByteArray {
        TODO("Not yet implemented")
    }
}