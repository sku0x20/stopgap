package dev.sku20.ir.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.sku20.ir.Creates

class IrSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private val symbolsCapturer = SymbolsCapturer<KSFunctionDeclaration>()

    @Suppress("UNCHECKED_CAST")
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation(Creates::class.qualifiedName!!)
            .toList() as List<KSFunctionDeclaration>

        symbolsCapturer.capture(symbols)

        if (symbols.isEmpty()) {
            generateFile()
            return emptyList()
        }

        return emptyList()
    }

    private val packageName = "dev.sku20.ir.generated"
    private val fileName = "IrInitCreators"

    private fun generateFile() {
        val symbols = symbolsCapturer.getSymbols()
        val file = codeGenerator.createNewFile(
            Dependencies(true),
            packageName,
            fileName,
            "kt"
        )
        codeGenerator.associateWithFunctions(
            symbols,
            packageName,
            fileName,
            "kt"
        )
        val initWriter = InitCreatorsWriter(
            file,
            symbols,
            packageName,
            fileName
        )
        initWriter.write()
    }

}