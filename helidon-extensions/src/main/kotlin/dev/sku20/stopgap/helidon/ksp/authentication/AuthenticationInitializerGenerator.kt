package dev.sku20.stopgap.helidon.ksp.authentication

import dev.sku20.stopgap.codegen.AstPrinter
import dev.sku20.stopgap.codegen.ast.*
import dev.sku20.stopgap.helidon.authentication.AuthenticationFilter
import dev.sku20.stopgap.helidon.authentication.AuthenticationResolver
import io.helidon.webserver.http.HttpRouting
import java.io.OutputStream

class AuthenticationInitializerGenerator(
    private val file: OutputStream,
    private val packageName: String = GeneratedNames.PACKAGE
) {

    fun write() {
        file.buffered().use { out ->
            AstPrinter(out).visitFile(astFile())
        }
    }

    private fun astFile(): AstFile {
        val resolver = AstLiteral(GeneratedNames.RESOLVER)
        val routes = AstLiteral(GeneratedNames.ROUTES)
        return AstFile(
            AstLiteral(packageName),
            listOf(
                AstFunction(
                    "initAuthentication",
                    listOf(
                        AstParam(resolver, AstType(AuthenticationResolver::class.qualifiedName!!)),
                        AstParam(routes, AstType(HttpRouting.Builder::class.qualifiedName!!)),
                    ),
                    listOf(
                        AstCall(
                            AstLiteral("addFilter"),
                            listOf(AstCall(AstType(AuthenticationFilter::class.qualifiedName!!), listOf(resolver))),
                            routes
                        )
                    )
                )
            )
        )
    }
}
