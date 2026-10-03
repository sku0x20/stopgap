package dev.sku20.stopgap.helidon.ksp.authentication

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import dev.sku20.stopgap.codegen.AstPrinter
import dev.sku20.stopgap.codegen.ast.AstCall
import dev.sku20.stopgap.codegen.ast.AstFile
import dev.sku20.stopgap.codegen.ast.AstFunction
import dev.sku20.stopgap.codegen.ast.AstLiteral
import dev.sku20.stopgap.codegen.ast.AstParam
import dev.sku20.stopgap.codegen.ast.AstType
import dev.sku20.stopgap.helidon.authentication.AuthenticationFilter
import dev.sku20.stopgap.helidon.authentication.AuthenticationResolver
import io.helidon.webserver.http.HttpRouting

class AuthenticationSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    private var generated = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (generated) return emptyList()
        generateFile()
        generated = true
        return emptyList()
    }

    private fun generateFile() {
        val file = codeGenerator.createNewFile(
            Dependencies(false),
            GeneratedNames.PACKAGE,
            GeneratedNames.FILE_NAME,
            GeneratedNames.EXTENSION
        )
        file.buffered().use { out ->
            AstPrinter(out)
                .visitFile(createAstFile())
        }
    }

    private fun createAstFile(): AstFile {
        val resolver = AstLiteral("resolver")
        val routes = AstLiteral("routes")

        return AstFile(
            AstLiteral(GeneratedNames.PACKAGE),
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
