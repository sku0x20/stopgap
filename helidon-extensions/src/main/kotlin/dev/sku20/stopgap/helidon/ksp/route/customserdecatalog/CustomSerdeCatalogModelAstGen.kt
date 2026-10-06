package dev.sku20.stopgap.helidon.ksp.route.customserdecatalog

import dev.sku20.stopgap.codegen.ast.*
import dev.sku20.stopgap.helidon.ksp.registry.RegistryQualifier
import dev.sku20.stopgap.helidon.serde.SerdeCatalog
import dev.sku20.stopgap.helidon.serde.SerdeExtras

class CustomSerdeCatalogModelAstGen(model: CustomSerdeCatalogModel?) {

    private val qualifier = if (model == null) SerdeExtras.DEFAULT_CATALOG_QUALIFIER else model.qualifier
    private val type = model?.type
    private val name = AstLiteral((qualifier ?: type!!).replace('.', '_'))

    fun name(): AstLiteral = name

    fun param(): AstParam {
        if (qualifier != null) {
            return AstParam(
                name,
                AstType(SerdeCatalog::class.qualifiedName!!),
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
