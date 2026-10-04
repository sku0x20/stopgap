package dev.sku20.stopgap.helidon.ksp.authentication

import dev.sku20.stopgap.codegen.ast.AstCall
import dev.sku20.stopgap.codegen.ast.AstFile
import dev.sku20.stopgap.codegen.ast.AstFunction
import dev.sku20.stopgap.codegen.ast.AstLiteral
import dev.sku20.stopgap.codegen.ast.AstParam
import dev.sku20.stopgap.codegen.ast.AstType
import dev.sku20.stopgap.helidon.authentication.AuthenticationFilter
import dev.sku20.stopgap.helidon.authentication.AuthenticationResolver
import io.helidon.webserver.http.HttpRouting

class AuthenticationAstGen(private val packageName: String) {

    private val resolver = AstLiteral("resolver")
    private val routes = AstLiteral("routes")

    fun file(): AstFile = AstFile(
        AstLiteral(packageName),
        listOf(initAuthenticationFn())
    )

    private fun initAuthenticationFn(): AstFunction = AstFunction(
        "initAuthentication",
        listOf(
            AstParam(
                resolver,
                AstType(AuthenticationResolver::class.qualifiedName!!)
            ),
            AstParam(
                routes,
                AstType(HttpRouting.Builder::class.qualifiedName!!)
            ),
        ),
        listOf(
            AstCall(
                AstLiteral("addFilter"),
                listOf(
                    AstCall(
                        AstType(AuthenticationFilter::class.qualifiedName!!),
                        listOf(resolver)
                    )
                ),
                routes
            )
        )
    )
}
