package com.example.stopgap.serde.plain

import com.example.stopgap.serde.ReqDto
import com.example.stopgap.serde.ResDto
import dev.sku20.helidon.serde.Serde
import io.helidon.http.HttpMediaType
import io.helidon.http.HttpMediaTypes
import kotlin.reflect.KClass
import kotlin.reflect.KType

class ReqResPlainSerde : Serde {

    override val mediaType: HttpMediaType = HttpMediaTypes.PLAINTEXT_UTF_8

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> deserialize(bytes: ByteArray, kClazz: KClass<T>): T {
        return ReqDto(String(bytes)) as T
    }

    override fun <T> deserialize(bytes: ByteArray, type: KType): T {
        throw UnsupportedOperationException()
    }

    override fun <T> serialize(obj: T): ByteArray {
        return (obj as ResDto).data.toByteArray()
    }
}
