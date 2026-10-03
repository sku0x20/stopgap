package dev.sku20.stopgap.codegen

import dev.sku20.stopgap.codegen.ast.AstFile
import dev.sku20.stopgap.codegen.ast.AstLiteral
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class AstPrinterTest {

    private val out = ByteArrayOutputStream()
    private val printer = AstPrinter(out)

    @Test
    fun astFileSimple() {
        val file = AstFile(
            AstLiteral("dev.sku20.example"),
            emptyList()
        )
        printer.visitFile(file)

        assertThat(out.toString())
            .isEqualTo("package dev.sku20.example\n")
    }

    @Test
    fun astLiteral() {
        val literal = AstLiteral("42")
        printer.visitLiteral(literal)

        assertThat(out.toString())
            .isEqualTo("42")
    }

}
