package dev.sku20.stopgap.helidon.ksp.route.customresmapper

import dev.sku20.stopgap.codegen.ast.*
import dev.sku20.stopgap.helidon.ksp.registry.RegistryQualifier
import dev.sku20.stopgap.helidon.mapper.res.CustomResMapper
import dev.sku20.stopgap.helidon.mapper.res.ResMapper

class CustomResMapperModelAstGen(model: CustomResMapperModel?) {

    private val qualifier = if (model == null) CustomResMapper.DEFAULT_QUALIFIER else model.qualifier
    private val type = model?.type
    private val name = AstLiteral((qualifier ?: type!!).replace('.', '_'))

    fun name(): AstLiteral = name

    fun param(): AstParam {
        if (qualifier != null) {
            return AstParam(
                name,
                AstType(ResMapper::class.qualifiedName!!),
                listOf(
                    AstAnnotation(
                        AstLiteral(RegistryQualifier::class.qualifiedName!!),
                        listOf(AstStringLiteral(qualifier))
                    )
                )
            )
        }
        return AstParam(name, AstType(type!!))
    }
}
