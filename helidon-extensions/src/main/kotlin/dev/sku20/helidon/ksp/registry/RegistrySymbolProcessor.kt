package dev.sku20.helidon.ksp.registry

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import dev.sku20.helidon.ksp.endpoint.EndpointSymbolProcessor

class RegistrySymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    companion object {
        const val GENERATED_PACKAGE = "dev.sku20.helidon.registry.generated"
        const val GENERATED_FILE_NAME = "RegistryInitializer"
        const val GENERATED_EXTENSION = "kt"
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (skip()) return emptyList()
        val endpointGeneratedFile = findEndpointInitializers(resolver.getNewFiles())
            ?: return emptyList()
        generateFile(endpointGeneratedFile)
        return emptyList()
    }

    private fun skip() = options["endpoint.codegen.registry.skip"].toBoolean()

    private fun findEndpointInitializers(files: Sequence<KSFile>): KSFile? = files.find {
        it.packageName.asString() == EndpointSymbolProcessor.GENERATED_PACKAGE &&
                it.fileName == "${EndpointSymbolProcessor.GENERATED_FILE_NAME}.${EndpointSymbolProcessor.GENERATED_EXTENSION}"
    }

    private fun generateFile(originatingFile: KSFile) {
        val file = codeGenerator.createNewFile(
            Dependencies(false, originatingFile),
            GENERATED_PACKAGE,
            GENERATED_FILE_NAME,
            GENERATED_EXTENSION
        )
        file.close()
    }
}
