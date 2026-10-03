package dev.sku20.stopgap.codegen

import dev.sku20.stopgap.codegen.ast.AstFile
import dev.sku20.stopgap.codegen.ast.AstLiteral
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class AstPrinterTest {

    @Test
    fun astFileSimple() {
        val out = ByteArrayOutputStream()

        val file = AstFile(
            AstLiteral("dev.sku20.example"),
            emptyList()
        )
        val printer = AstPrinter(out)
        printer.visitFile(file)

        assertThat(out.toString()).isEqualTo("package dev.sku20.example\n")
    }

}
