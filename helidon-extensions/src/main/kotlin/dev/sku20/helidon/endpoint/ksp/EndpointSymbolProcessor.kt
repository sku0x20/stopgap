package dev.sku20.helidon.endpoint.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import dev.sku20.helidon.endpoint.Endpoint

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

    private val packageName = "dev.sku20.helidon.endpoint.generated"
    private val fileName = "Initializers"
    private val extension = "kt"

    private fun generateFile(symbols: List<KSClassDeclaration>) {
        val file = codeGenerator.createNewFile(
            Dependencies(true),
            packageName,
            fileName,
            extension
        )
        codeGenerator.associateWithClasses(
            symbols,
            packageName,
            fileName,
            extension
        )
        val initGen = InitializersGenerator(
            file,
            symbols,
            packageName,
            !options["endpoint.codegen.registry.skip"].toBoolean()
        )
        initGen.write()
    }

}