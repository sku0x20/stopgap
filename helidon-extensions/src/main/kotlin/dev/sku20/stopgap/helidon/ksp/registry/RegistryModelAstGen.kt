package dev.sku20.stopgap.helidon.ksp.registry

import dev.sku20.stopgap.codegen.ast.AstCall
import dev.sku20.stopgap.codegen.ast.AstExpression
import dev.sku20.stopgap.codegen.ast.AstFile
import dev.sku20.stopgap.codegen.ast.AstFunction
import dev.sku20.stopgap.codegen.ast.AstLiteral
import dev.sku20.stopgap.codegen.ast.AstParam
import dev.sku20.stopgap.codegen.ast.AstStringLiteral
import dev.sku20.stopgap.codegen.ast.AstType
import dev.sku20.stopgap.helidon.authentication.AuthenticationResolver
import dev.sku20.stopgap.helidon.ksp.authentication.GeneratedNames as AuthGeneratedNames
import dev.sku20.stopgap.helidon.ksp.endpoint.GeneratedNames as EndpointGeneratedNames
import io.helidon.webserver.http.HttpRouting

class RegistryModelAstGen(
    private val model: RegistryModel,
    private val packageName: String
) {

    private val registry = AstLiteral("registry")
    private val routes = AstLiteral("routes")

    fun file(): AstFile = AstFile(
        AstLiteral(packageName),
        listOf(initHttpRoutingFn())
    )

    private fun initHttpRoutingFn(): AstFunction {
        val content = mutableListOf<AstExpression>()
        content.add(initAuthenticationCall())
        for (endpoint in model.endpoints) {
            content.add(registerRoutesCall(endpoint))
        }
        return AstFunction(
            "initHttpRouting",
            listOf(
                AstParam(registry, AstType(INSTANCE_REGISTRY)),
                AstParam(routes, AstType(HttpRouting.Builder::class.qualifiedName!!)),
            ),
            content
        )
    }

    private fun initAuthenticationCall(): AstCall = AstCall(
        AstLiteral("${AuthGeneratedNames.PACKAGE}.initAuthentication"),
        listOf(getInstanceForType(AuthenticationResolver::class.qualifiedName!!), routes)
    )

    private fun registerRoutesCall(endpoint: RegistryModel.Endpoint): AstCall {
        val arguments = mutableListOf<AstExpression>()
        arguments.add(getInstanceForType(endpoint.type))
        arguments.add(routes)
        for (catalog in endpoint.catalogs) {
            arguments.add(catalogArgument(catalog))
        }
        return AstCall(AstLiteral("${EndpointGeneratedNames.PACKAGE}.registerRoutesFor"), arguments)
    }

    private fun catalogArgument(catalog: RegistryModel.Catalog): AstCall {
        if (catalog.qualifier != null) {
            return AstCall(
                AstLiteral("getInstanceForQualifier"),
                listOf(AstStringLiteral(catalog.qualifier)),
                registry
            )
        }
        return getInstanceForType(catalog.type!!)
    }

    private fun getInstanceForType(type: String): AstCall = AstCall(
        AstLiteral("getInstanceForType"),
        emptyList(),
        registry,
        listOf(AstType(type))
    )

    private companion object {
        const val INSTANCE_REGISTRY = "dev.sku20.stopgap.ir.InstanceRegistry"
    }
}
