package dev.sku20.helidon.ksp.endpoint

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSType
import dev.sku20.helidon.serde.SerdeExtras

// stand-in for @CustomSerdeCatalog; can't instantiate the annotation type itself since `clazz` is only available as a KSType pre-compilation
class CustomSerdeCatalogInfo(annotation: KSAnnotation?) {
    val qualifier: String = annotation?.argument("qualifier") as? String
        ?: SerdeExtras.DEFAULT_CATALOG_QUALIFIER
    val clazz: KSType? = annotation?.argument("clazz") as? KSType

    fun asAnnotationString(): String = if (qualifier.isNotEmpty()) {
        "@CustomSerdeCatalog(\"$qualifier\")"
    } else {
        "@CustomSerdeCatalog(clazz=${clazz!!.declaration.qualifiedName!!.asString()}::class)"
    }

    companion object {
        private fun KSAnnotation.argument(name: String): Any? =
            arguments.firstOrNull { it.name?.getShortName() == name }?.value
    }
}
