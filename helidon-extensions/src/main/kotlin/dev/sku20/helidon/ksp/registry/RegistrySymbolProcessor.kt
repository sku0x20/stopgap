package dev.sku20.helidon.ksp.registry

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated

class RegistrySymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (resolver.getNewFiles().none()) return emptyList()
        generateFile()
        return emptyList()
    }

    private val packageName = "dev.sku20.helidon.registry.generated"
    private val fileName = "RegistryInitializer"
    private val extension = "kt"

    private fun generateFile() {
        val file = codeGenerator.createNewFile(
            Dependencies(false),
            packageName,
            fileName,
            extension
        )
        file.close()
    }
}
