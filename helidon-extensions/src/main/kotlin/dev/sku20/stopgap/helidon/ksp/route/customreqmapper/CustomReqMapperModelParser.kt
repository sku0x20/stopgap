package dev.sku20.stopgap.helidon.ksp.route.customreqmapper

import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import dev.sku20.stopgap.helidon.ksp.argument
import dev.sku20.stopgap.helidon.ksp.findAnnotation
import dev.sku20.stopgap.helidon.mapper.req.CustomReqMapper
import dev.sku20.stopgap.helidon.mapper.req.ReqMapper

class CustomReqMapperModelParser(private val declaration: KSDeclaration) {

    fun parse(): CustomReqMapperModel? {
        val annotation = declaration
            .findAnnotation(CustomReqMapper::class) ?: return null
        val qualifier = annotation.argument<String>("qualifier")
        val clazz = annotation.argument<KSType>("clazz").declaration.qualifiedName!!.asString()
        val model = CustomReqMapperModel(
            if (clazz == ReqMapper::class.qualifiedName) null else clazz,
            if (qualifier.isEmpty()) null else qualifier,
        )
        if (model.type == null && model.qualifier == null) {
            throw IllegalArgumentException("CustomReqMapper needs a qualifier or clazz on: ${declaration.qualifiedName!!.asString()}")
        }
        return model
    }
}
