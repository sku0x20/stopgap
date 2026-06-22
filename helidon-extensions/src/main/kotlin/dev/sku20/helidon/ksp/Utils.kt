package dev.sku20.helidon.ksp

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.reflect.KClass

object Utils {
    fun capturing(block: (CustomWriter) -> Unit): InputStream {
        val buffer = ByteArrayOutputStream()
        val writer = CustomWriter(buffer)
        block(writer)
        writer.close()
        return buffer.toByteArray().inputStream()
    }
}

fun KSAnnotated.findAnnotation(klass: KClass<*>): KSAnnotation? =
    annotations.firstOrNull { it.shortName.asString() == klass.simpleName }

@Suppress("UNCHECKED_CAST")
fun <T> KSAnnotation.argument(name: String): T =
    arguments.first { it.name?.getShortName() == name }.value as T
