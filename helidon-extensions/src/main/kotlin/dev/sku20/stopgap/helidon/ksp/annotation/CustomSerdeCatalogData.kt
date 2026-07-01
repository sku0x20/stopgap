package dev.sku20.stopgap.helidon.ksp.annotation

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSType
import dev.sku20.stopgap.helidon.ksp.argument
import dev.sku20.stopgap.helidon.ksp.findAnnotation
import dev.sku20.stopgap.helidon.serde.CustomSerdeCatalog
import dev.sku20.stopgap.helidon.serde.SerdeExtras

class CustomSerdeCatalogData(
    val qualifier: String? = null,
    val clazz: KSType? = null
) {
    fun asAnnotationString(): String = if (!qualifier.isNullOrEmpty()) {
        "@CustomSerdeCatalog(\"$qualifier\")"
    } else {
        "@CustomSerdeCatalog(clazz=${clazz!!.declaration.qualifiedName!!.asString()}::class)"
    }

    fun paramName(): String {
        val raw = if (!qualifier.isNullOrEmpty()) qualifier else clazz!!.declaration.qualifiedName!!.asString()
        return raw.replace('.', '_')
    }

    companion object {
        fun from(annotated: KSAnnotated) =
            fromOrDefault(annotated, CustomSerdeCatalogData(SerdeExtras.DEFAULT_CATALOG_QUALIFIER))

        fun fromOrDefault(annotated: KSAnnotated, default: CustomSerdeCatalogData): CustomSerdeCatalogData {
            val kSAnnotation = annotated.findAnnotation(CustomSerdeCatalog::class)
                ?: return default
            return parse(kSAnnotation)
        }

        private fun parse(annotation: KSAnnotation): CustomSerdeCatalogData = CustomSerdeCatalogData(
            annotation.argument("qualifier"),
            annotation.argument("clazz")
        )
    }
}
