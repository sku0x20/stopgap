package com.example.stopgap.serde.fastjson

import com.alibaba.fastjson2.JSON
import dev.sku20.helidon.serde.Serde
import io.helidon.http.HttpMediaTypes
import io.helidon.http.ServerResponseHeaders
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.javaType

class FastjsonSerde : Serde {

    override fun <T : Any> deserialize(bytes: ByteArray, kClazz: KClass<T>): T {
        return JSON.parseObject(bytes, kClazz.java)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun <T> deserialize(bytes: ByteArray, type: KType): T {
        return JSON.parseObject(bytes, type.javaType)
    }

    override fun <T> serialize(obj: T): ByteArray {
        return JSON.toJSONBytes(obj)
    }

    override fun setHeaders(headers: ServerResponseHeaders) {
        headers.contentType(HttpMediaTypes.JSON_UTF_8)
    }
}
