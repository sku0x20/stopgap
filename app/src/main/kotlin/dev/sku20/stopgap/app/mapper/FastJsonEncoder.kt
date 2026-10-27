package dev.sku20.stopgap.app.mapper

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.writer.ObjectWriter
import dev.sku20.stopgap.helidon.mapper.res.bodyresmapper.BodyEncoder
import kotlin.uuid.Uuid

class FastJsonEncoder : BodyEncoder {

    init {
        // kotlin Uuid as string, not its bit fields
        JSON.register(Uuid::class.java, ObjectWriter<Uuid> { writer, obj, _, _, _ -> writer.writeString(obj.toString()) })
    }

    override fun <T> encode(value: T): ByteArray = JSON.toJSONBytes(value)
}
