package dev.sku20.stopgap.helidon.ksp.route

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import dev.sku20.stopgap.helidon.endpoint.Endpoint
import dev.sku20.stopgap.helidon.ksp.endpoint.EndpointRoutesGenerator
import dev.sku20.stopgap.helidon.ksp.endpoint.GeneratedNames

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
        val routesGen = EndpointRoutesGenerator(
            file,
            symbols,
            GeneratedNames.PACKAGE,
            options[DEFAULT_AUTH_TYPE_OPTION]
        )
        routesGen.write()
    }
}
