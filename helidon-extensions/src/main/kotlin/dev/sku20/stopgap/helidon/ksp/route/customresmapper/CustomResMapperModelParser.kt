package dev.sku20.stopgap.helidon.ksp.route.customresmapper

import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import dev.sku20.stopgap.helidon.ksp.argument
import dev.sku20.stopgap.helidon.ksp.findAnnotation
import dev.sku20.stopgap.helidon.mapper.res.CustomResMapper
import dev.sku20.stopgap.helidon.mapper.res.ResMapper

class CustomResMapperModelParser(private val declaration: KSDeclaration) {

    fun parse(): CustomResMapperModel? {
        val annotation = declaration
            .findAnnotation(CustomResMapper::class) ?: return null
        val qualifier = annotation.argument<String>("qualifier")
        val clazz = annotation.argument<KSType>("clazz").declaration.qualifiedName!!.asString()
        val model = CustomResMapperModel(
            if (clazz == ResMapper::class.qualifiedName) null else clazz,
            if (qualifier.isEmpty()) null else qualifier,
        )
        if (model.type == null && model.qualifier == null) {
            throw IllegalArgumentException("CustomResMapper needs a qualifier or clazz on: ${declaration.qualifiedName!!.asString()}")
        }
        return model
    }
}
