package dev.sku20.helidon.ksp.endpoint.annotation

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import dev.sku20.helidon.ksp.argument
import dev.sku20.helidon.ksp.findAnnotation
import dev.sku20.helidon.serde.CustomSerdeCatalog
import dev.sku20.helidon.serde.SerdeExtras

class CustomSerdeCatalogData(
    private val qualifier: String? = null,
    private val clazz: KSType? = null
) {
    fun asAnnotationString(): String = if (!qualifier.isNullOrEmpty()) {
        "@CustomSerdeCatalog(\"$qualifier\")"
    } else {
        "@CustomSerdeCatalog(clazz=${clazz!!.declaration.qualifiedName!!.asString()}::class)"
    }

    companion object {
        fun from(clazzDecl: KSClassDeclaration): CustomSerdeCatalogData {
            val kSAnnotation = clazzDecl.findAnnotation(CustomSerdeCatalog::class)
                ?: return CustomSerdeCatalogData(SerdeExtras.DEFAULT_CATALOG_QUALIFIER)
            return CustomSerdeCatalogData(
                kSAnnotation.argument("qualifier"),
                kSAnnotation.argument("clazz")
            )
        }
    }
}
