package dev.sku20.stopgap.helidon.ksp.customserdecatalog

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSType
import dev.sku20.stopgap.helidon.ksp.argument
import dev.sku20.stopgap.helidon.ksp.findAnnotation
import dev.sku20.stopgap.helidon.serde.CustomSerdeCatalog

class CustomSerdeCatalogModelParser(private val annotated: KSAnnotated) {

    fun parse(): CustomSerdeCatalogModel? {
        val annotation = annotated
            .findAnnotation(CustomSerdeCatalog::class) ?: return null
        val qualifier = annotation.argument<String>("qualifier")
        val clazz = annotation.argument<KSType>("clazz").declaration.qualifiedName!!.asString()
        return CustomSerdeCatalogModel(
            if (clazz == Unit::class.qualifiedName) null else clazz,
            if (qualifier.isEmpty()) null else qualifier,
        )
    }
}
