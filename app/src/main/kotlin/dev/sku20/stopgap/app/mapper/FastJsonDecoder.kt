package dev.sku20.stopgap.app.mapper

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONException
import com.alibaba.fastjson2.reader.ObjectReader
import dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.BodyDecoder
import dev.sku20.stopgap.helidon.mapper.req.exception.MalformedBodyException
import kotlin.reflect.KType
import kotlin.reflect.javaType
import kotlin.uuid.Uuid

object FastJsonDecoder : BodyDecoder {

    init {
        // kotlin Uuid as string, not its bit fields
        JSON.register(Uuid::class.java, ObjectReader<Uuid> { reader, _, _, _ -> Uuid.parse(reader.readString()) })
    }

    // A whitespace-only or `null` body parses to null, which a non-null type must reject.
    override fun <T> decode(bytes: ByteArray, type: KType): T {
        val value = parse<T>(bytes, type)
        if (value == null && !type.isMarkedNullable) {
            throw MalformedBodyException(null)
        }
        return value
    }

    // A missing non-null field surfaces as an NPE from the Kotlin constructor's null check, not a JSONException.
    @OptIn(ExperimentalStdlibApi::class)
    private fun <T> parse(bytes: ByteArray, type: KType): T {
        try {
            return JSON.parseObject(bytes, type.javaType)
        } catch (e: JSONException) {
            throw MalformedBodyException(e)
        } catch (e: NullPointerException) {
            throw MalformedBodyException(e)
        }
    }
}
