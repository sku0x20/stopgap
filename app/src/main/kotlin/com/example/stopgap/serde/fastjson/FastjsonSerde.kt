package com.example.stopgap.serde.fastjson

import com.alibaba.fastjson2.JSON
import dev.sku20.stopgap.helidon.serde.Serde
import io.helidon.http.HttpMediaType
import io.helidon.http.HttpMediaTypes
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.javaType

class FastjsonSerde : Serde {

    override val mediaType: HttpMediaType = HttpMediaTypes.JSON_UTF_8

    override fun <T : Any> deserialize(bytes: ByteArray, clazz: KClass<T>): T {
        return JSON.parseObject(bytes, clazz.java)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun <T> deserialize(bytes: ByteArray, type: KType): T {
        return JSON.parseObject(bytes, type.javaType)
    }

    override fun <T> serialize(obj: T): ByteArray {
        return JSON.toJSONBytes(obj)
    }
}
