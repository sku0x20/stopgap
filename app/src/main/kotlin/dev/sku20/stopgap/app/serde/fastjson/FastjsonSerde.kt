package dev.sku20.stopgap.app.serde.fastjson

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONException
import com.alibaba.fastjson2.reader.ObjectReader
import com.alibaba.fastjson2.writer.ObjectWriter
import dev.sku20.stopgap.helidon.serde.Deserializer
import dev.sku20.stopgap.helidon.serde.deprecated.SerdeDeprecated
import io.helidon.http.HttpMediaType
import io.helidon.http.HttpMediaTypes
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.javaType
import kotlin.uuid.Uuid

class FastjsonSerde : SerdeDeprecated, Deserializer {

    override val mediaType: HttpMediaType = HttpMediaTypes.JSON_UTF_8

    init {
        // kotlin Uuid as string, not its bit fields
        JSON.register(
            Uuid::class.java,
            ObjectWriter<Uuid> { writer, obj, _, _, _ -> writer.writeString(obj.toString()) })
        JSON.register(Uuid::class.java, ObjectReader<Uuid> { reader, _, _, _ -> Uuid.parse(reader.readString()) })
    }

    // RFC 8259: JSON is always UTF-8 and defines no charset parameter, but clients often send charset=utf-8.
    // Helidon keeps type, subtype and charset as sent, and all three are case-insensitive.
    override fun canDeserialize(mediaType: HttpMediaType): Boolean {
        val isJson = mediaType.type().equals("application", true) && mediaType.subtype().equals("json", true)
        return isJson && mediaType.charset().orElse("utf-8").equals("utf-8", true)
    }

    override fun <T : Any> deserialize(bytes: ByteArray, clazz: KClass<T>): T {
        // An empty body parses to null.
        return parse(bytes, clazz.java) ?: throw MalformedBodyException(null)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun <T> deserialize(bytes: ByteArray, type: KType): T {
        return parse(bytes, type.javaType)
    }

    // A missing non-null field surfaces as an NPE from the Kotlin constructor's null check, not a JSONException.
    private fun <T> parse(bytes: ByteArray, type: Type): T {
        try {
            return JSON.parseObject(bytes, type)
        } catch (e: JSONException) {
            throw MalformedBodyException(e)
        } catch (e: NullPointerException) {
            throw MalformedBodyException(e)
        }
    }

    override fun <T> serialize(obj: T): ByteArray {
        return JSON.toJSONBytes(obj)
    }
}
