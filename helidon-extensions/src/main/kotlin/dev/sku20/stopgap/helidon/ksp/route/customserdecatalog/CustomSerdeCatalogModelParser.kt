package dev.sku20.stopgap.helidon.ksp.route.customserdecatalog

import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import dev.sku20.stopgap.helidon.ksp.argument
import dev.sku20.stopgap.helidon.ksp.findAnnotation
import dev.sku20.stopgap.helidon.serde.CustomSerdeCatalog

class CustomSerdeCatalogModelParser(private val declaration: KSDeclaration) {

    fun parse(): CustomSerdeCatalogModel? {
        val annotation = declaration
            .findAnnotation(CustomSerdeCatalog::class) ?: return null
        val qualifier = annotation.argument<String>("qualifier")
        val clazz = annotation.argument<KSType>("clazz").declaration.qualifiedName!!.asString()
        val model = CustomSerdeCatalogModel(
            if (clazz == Unit::class.qualifiedName) null else clazz,
            if (qualifier.isEmpty()) null else qualifier,
        )
        if (model.type == null && model.qualifier == null) {
            throw IllegalArgumentException("CustomSerdeCatalog needs a qualifier or clazz on: ${declaration.qualifiedName!!.asString()}")
        }
        return model
    }
}
