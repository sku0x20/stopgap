package com.example.stopgap.serde.fastjson

import com.alibaba.fastjson2.JSON
import dev.sku20.helidon.serde.Serde
import kotlin.reflect.KClass

// todo: benchmark both Fastjson Serde implementations
// only supports non-generic types
// faster than generic one as avoid one if check.
class FastjsonSerde : Serde {

    override fun <T : Any> deserialize(bytes: ByteArray, kClazz: KClass<T>): T {
        return JSON.parseObject(bytes, kClazz.java)
    }

    override fun <T : Any> serialize(obj: T): ByteArray {
        return JSON.toJSONBytes(obj)
    }
}
