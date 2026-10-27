package dev.sku20.stopgap.helidon.ksp.route.customreqmapper

import dev.sku20.stopgap.codegen.ast.*
import dev.sku20.stopgap.helidon.ksp.registry.RegistryQualifier
import dev.sku20.stopgap.helidon.mapper.req.CustomReqMapper
import dev.sku20.stopgap.helidon.mapper.req.ReqMapper

class CustomReqMapperModelAstGen(model: CustomReqMapperModel?) {

    private val qualifier = if (model == null) CustomReqMapper.DEFAULT_QUALIFIER else model.qualifier
    private val type = model?.type
    private val name = AstLiteral((qualifier ?: type!!).replace('.', '_'))

    fun name(): AstLiteral = name

    fun param(): AstParam {
        if (qualifier != null) {
            return AstParam(
                name,
                AstType(ReqMapper::class.qualifiedName!!),
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
