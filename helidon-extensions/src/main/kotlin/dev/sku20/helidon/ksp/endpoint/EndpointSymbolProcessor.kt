package dev.sku20.helidon.ksp.endpoint

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

    private fun generateFile(symbols: List<KSClassDeclaration>) {
        val file = codeGenerator.createNewFile(
            Dependencies(true),
            GeneratedNames.GENERATED_PACKAGE,
            GeneratedNames.GENERATED_FILE_NAME,
            GeneratedNames.GENERATED_EXTENSION
        )
        codeGenerator.associateWithClasses(
            symbols,
            GeneratedNames.GENERATED_PACKAGE,
            GeneratedNames.GENERATED_FILE_NAME,
            GeneratedNames.GENERATED_EXTENSION
        )
        val initGen = InitializersGenerator(
            file,
            symbols,
            GeneratedNames.GENERATED_PACKAGE
        )
        initGen.write()
    }

}
