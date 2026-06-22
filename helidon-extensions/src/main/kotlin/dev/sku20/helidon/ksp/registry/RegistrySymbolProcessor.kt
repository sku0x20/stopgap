package dev.sku20.helidon.ksp.registry

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.sku20.helidon.ksp.endpoint.GeneratedNames as EndpointGeneratedNames

class RegistrySymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (skip()) return emptyList()
        val endpointGeneratedFile = findEndpointInitializers(resolver.getNewFiles())
            ?: return emptyList()
        generateFile(endpointGeneratedFile)
        return emptyList()
    }

    private fun skip() = options["endpoint.codegen.registry.skip"].toBoolean()

    private fun findEndpointInitializers(files: Sequence<KSFile>): KSFile? = files.find {
        it.packageName.asString() == EndpointGeneratedNames.GENERATED_PACKAGE &&
                it.fileName == "${EndpointGeneratedNames.GENERATED_FILE_NAME}.${EndpointGeneratedNames.GENERATED_EXTENSION}"
    }

    private fun generateFile(originatingFile: KSFile) {
        val functions = originatingFile.declarations.filterIsInstance<KSFunctionDeclaration>().toList()
        val file = codeGenerator.createNewFile(
            Dependencies(false, originatingFile),
            GeneratedNames.GENERATED_PACKAGE,
            GeneratedNames.GENERATED_FILE_NAME,
            GeneratedNames.GENERATED_EXTENSION
        )
        RegistryInitializerGenerator(file, functions, GeneratedNames.GENERATED_PACKAGE)
            .write()
    }
}
