package dev.sku20.helidon.ksp.endpoint

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import dev.sku20.helidon.serde.CustomSerdeCatalog
import dev.sku20.helidon.serde.SerdeExtras

class CustomSerdeCatalogInfo(
    private val qualifier: String? = null,
    private val clazz: KSType? = null
) {
    fun asAnnotationString(): String = if (!qualifier.isNullOrEmpty()) {
        "@CustomSerdeCatalog(\"$qualifier\")"
    } else {
        "@CustomSerdeCatalog(clazz=${clazz!!.declaration.qualifiedName!!.asString()}::class)"
    }

    companion object {

        fun from(clazzDecl: KSClassDeclaration): CustomSerdeCatalogInfo {
            val kSAnnotation = clazzDecl.annotations.firstOrNull {
                it.shortName.asString() == CustomSerdeCatalog::class.simpleName
            }
            if (kSAnnotation == null) {
                return CustomSerdeCatalogInfo(SerdeExtras.DEFAULT_CATALOG_QUALIFIER)
            }
            return CustomSerdeCatalogInfo(
                kSAnnotation.argument("qualifier"),
                kSAnnotation.argument("clazz")
            )
        }

        @Suppress("UNCHECKED_CAST")
        private fun <T> KSAnnotation.argument(name: String): T =
            arguments.firstOrNull { it.name?.getShortName() == name }?.value as T
    }
}
