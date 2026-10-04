package dev.sku20.stopgap.ir.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.sku20.stopgap.ir.Creates

class IrSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    @Suppress("UNCHECKED_CAST")
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation(Creates::class.qualifiedName!!)
            .toList() as List<KSFunctionDeclaration>
        if (symbols.isEmpty()) return emptyList()
        generateFile(symbols)
        return emptyList()
    }

    private fun generateFile(symbols: List<KSFunctionDeclaration>) {
        val file = codeGenerator.createNewFile(
            Dependencies(true),
            GeneratedNames.PACKAGE,
            GeneratedNames.FILE_NAME,
            GeneratedNames.EXTENSION
        )
        codeGenerator.associateWithFunctions(
            symbols,
            GeneratedNames.PACKAGE,
            GeneratedNames.FILE_NAME,
            GeneratedNames.EXTENSION
        )
        val initWriter = InitializersGenerator(
            file,
            symbols,
            GeneratedNames.PACKAGE
        )
        initWriter.write()
    }

}