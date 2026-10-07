package dev.sku20.stopgap.helidon.ksp.route

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import dev.sku20.stopgap.codegen.AstPrinter
import dev.sku20.stopgap.codegen.ast.AstExpression
import dev.sku20.stopgap.codegen.ast.AstFile
import dev.sku20.stopgap.codegen.ast.AstLiteral
import dev.sku20.stopgap.helidon.endpoint.Endpoint
import dev.sku20.stopgap.helidon.ksp.route.endpoint.EndpointModelAstGen
import dev.sku20.stopgap.helidon.ksp.route.endpoint.EndpointModelParser

class EndpointSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    @Suppress("UNCHECKED_CAST")
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation(Endpoint::class.qualifiedName!!)
            .toList() as List<KSClassDeclaration>
        if (symbols.isEmpty()) return emptyList()
        generateFile(symbols)
        return emptyList()
    }

    companion object {
        const val DEFAULT_AUTH_TYPE_OPTION = "stopgap.codegen.endpoint.auth.defaultType"
    }

    private fun generateFile(symbols: List<KSClassDeclaration>) {
        val astFile = AstFile(AstLiteral(GeneratedNames.PACKAGE), functions(symbols))
        val file = codeGenerator.createNewFile(
            Dependencies(true),
            GeneratedNames.PACKAGE,
            GeneratedNames.FILE_NAME,
            GeneratedNames.EXTENSION
        )
        codeGenerator.associateWithClasses(
            symbols,
            GeneratedNames.PACKAGE,
            GeneratedNames.FILE_NAME,
            GeneratedNames.EXTENSION
        )
        file.buffered().use { out ->
            AstPrinter(out).visitFile(astFile)
        }
    }

    private fun functions(symbols: List<KSClassDeclaration>): List<AstExpression> {
        val defaultAuthType = options[DEFAULT_AUTH_TYPE_OPTION]
        val functions = mutableListOf<AstExpression>()
        for (symbol in symbols) {
            val model = EndpointModelParser(symbol).parse()
            functions.add(EndpointModelAstGen(model, defaultAuthType).function())
        }
        return functions
    }
}
