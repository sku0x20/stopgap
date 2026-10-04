package dev.sku20.stopgap.helidon.ksp.authentication

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import dev.sku20.stopgap.codegen.AstPrinter

class AuthenticationSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    private var generated = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (generated) return emptyList()
        generateFile()
        generated = true
        return emptyList()
    }

    private fun generateFile() {
        val astFile = AuthenticationAstGen(GeneratedNames.PACKAGE).file()
        val file = codeGenerator.createNewFile(
            Dependencies(false),
            GeneratedNames.PACKAGE,
            GeneratedNames.FILE_NAME,
            GeneratedNames.EXTENSION
        )
        file.buffered().use { out ->
            AstPrinter(out)
                .visitFile(astFile)
        }
    }
}
