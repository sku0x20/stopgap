package dev.sku20.stopgap.helidon.ksp.endpoint

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import dev.sku20.stopgap.helidon.endpoint.Endpoint
import dev.sku20.stopgap.helidon.ksp.authentication.AuthenticationInitializerGenerator
import dev.sku20.stopgap.helidon.ksp.authentication.GeneratedNames as AuthGeneratedNames

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
        generateEndpointRoutes(symbols)
        generateAuthenticationInitializer(symbols)
        return emptyList()
    }

    private fun generateEndpointRoutes(symbols: List<KSClassDeclaration>) {
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
            GeneratedNames.PACKAGE
        )
        routesGen.write()
    }

    private fun generateAuthenticationInitializer(symbols: List<KSClassDeclaration>) {
        val file = codeGenerator.createNewFile(
            Dependencies(true),
            AuthGeneratedNames.PACKAGE,
            AuthGeneratedNames.FILE_NAME,
            AuthGeneratedNames.EXTENSION
        )
        codeGenerator.associateWithClasses(
            symbols,
            AuthGeneratedNames.PACKAGE,
            AuthGeneratedNames.FILE_NAME,
            AuthGeneratedNames.EXTENSION
        )
        val authGen = AuthenticationInitializerGenerator(
            file,
            AuthGeneratedNames.PACKAGE
        )
        authGen.write()
    }
}
