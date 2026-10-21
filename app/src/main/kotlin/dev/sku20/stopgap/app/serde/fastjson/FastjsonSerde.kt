package dev.sku20.stopgap.app.serde.fastjson

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.reader.ObjectReader
import com.alibaba.fastjson2.writer.ObjectWriter
import dev.sku20.stopgap.helidon.serde.Serde
import io.helidon.http.HttpMediaType
import io.helidon.http.HttpMediaTypes
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.javaType
import kotlin.uuid.Uuid

class FastjsonSerde : Serde {

    override val mediaType: HttpMediaType = HttpMediaTypes.JSON_UTF_8

    init {
        // kotlin Uuid as string, not its bit fields
        JSON.register(Uuid::class.java, ObjectWriter<Uuid> { writer, obj, _, _, _ -> writer.writeString(obj.toString()) })
        JSON.register(Uuid::class.java, ObjectReader<Uuid> { reader, _, _, _ -> Uuid.parse(reader.readString()) })
    }

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
