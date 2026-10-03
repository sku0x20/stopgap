package dev.sku20.stopgap.codegen

import dev.sku20.stopgap.codegen.ast.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class AstPrinterTest {

    private val out = ByteArrayOutputStream()
    private val printer = AstPrinter(out)

    @Test
    fun astFile() {
        val file = AstFile(
            AstLiteral("dev.sku20.example"),
            listOf(AstLiteral("a"), AstLiteral("b"))
        )
        printer.visitFile(file)

        assertOutput(
            """
            package dev.sku20.example

            a

            b

            """.trimIndent()
        )
    }

    @Test
    fun astLiteral() {
        val literal = AstLiteral("42")
        printer.visitLiteral(literal)

        assertOutput("42")
    }

    @Test
    fun astStringLiteral() {
        val literal = AstStringLiteral("hello")
        printer.visitStringLiteral(literal)

        assertOutput("\"hello\"")
    }

    @Test
    fun astType() {
        val type = AstType("String", "kotlin.String")
        printer.visitType(type)

        assertOutput("kotlin.String")
    }

    @Test
    fun astParam() {
        printer.visitParam(AstParam(AstLiteral("a")))
        assertOutput("a")

        out.reset()

        printer.visitParam(
            AstParam(
                AstLiteral("a"),
                AstType("String", "kotlin.String")
            )
        )
        assertOutput("a: kotlin.String")
    }

    @Test
    fun astCall() {
        printer.visitCall(
            AstCall(
                AstLiteral("f"),
                listOf(
                    AstLiteral("a"),
                    AstLiteral("b")
                )
            )
        )
        assertOutput("f(a, b)")

        out.reset()

        printer.visitCall(
            AstCall(
                AstLiteral("f"),
                listOf(AstLiteral("a")),
                AstLiteral("r")
            )
        )
        assertOutput("r.f(a)")
    }

    @Test
    fun astReturn() {
        printer.visitReturn(AstReturn())
        assertOutput("return")

        out.reset()

        printer.visitReturn(AstReturn(AstLiteral("a")))
        assertOutput("return a")

        out.reset()

        printer.visitReturn(AstReturn(label = AstLiteral("l")))
        assertOutput("return@l")
    }

    @Test
    fun astAssignment() {
        printer.visitAssignment(
            AstAssignment(
                AstLiteral("a"),
                variableType = VariableType.VAL,
                value = AstLiteral("1")
            )
        )
        assertOutput("val a = 1")

        out.reset()

        printer.visitAssignment(
            AstAssignment(
                AstLiteral("a"),
                variableType = VariableType.VAR,
                value = AstLiteral("1")
            )
        )
        assertOutput("var a = 1")

        out.reset()

        printer.visitAssignment(
            AstAssignment(
                AstLiteral("a"),
                AstType("String", "kotlin.String"),
                VariableType.LATEINIT_VAR
            )
        )
        assertOutput("lateinit var a: kotlin.String")

        out.reset()

        printer.visitAssignment(
            AstAssignment(
                AstLiteral("a"),
                value = AstLiteral("1")
            )
        )
        assertOutput("a = 1")
    }

    @Test
    fun astFunction() {
        val function = AstFunction(
            "f",
            listOf(AstParam(AstLiteral("a")), AstParam(AstLiteral("b"))),
            listOf(AstLiteral("x"), AstLiteral("y"))
        )
        printer.visitFunction(function)

        assertOutput(
            """
            fun f(a, b) {
                x
                y
            }
            """.trimIndent()
        )
    }

    @Test
    fun astFunctionReturnType() {
        val function = AstFunction(
            "f",
            emptyList(),
            listOf(AstLiteral("x")),
            AstType("String", "kotlin.String")
        )
        printer.visitFunction(function)

        assertOutput(
            """
            fun f(): kotlin.String {
                x
            }
            """.trimIndent()
        )
    }

    @Test
    fun astLambda() {
        val lambda = AstLambda(
            listOf(AstParam(AstLiteral("a")), AstParam(AstLiteral("b"))),
            listOf(AstLiteral("x"), AstLiteral("y"))
        )
        printer.visitLambda(lambda)

        assertOutput(
            """
            { a, b ->
                x
                y
            }
            """.trimIndent()
        )
    }

    @Test
    fun astLambdaNoParams() {
        val lambda = AstLambda(emptyList(), listOf(AstLiteral("x")))
        printer.visitLambda(lambda)

        assertOutput(
            """
            {
                x
            }
            """.trimIndent()
        )
    }

    @Test
    fun nestedIndent() {
        val function = AstFunction(
            "f",
            emptyList(),
            listOf(AstCall(AstLiteral("run"), listOf(AstLambda(emptyList(), listOf(AstLiteral("x"))))))
        )
        printer.visitFunction(function)

        assertOutput(
            """
            fun f() {
                run({
                    x
                })
            }
            """.trimIndent()
        )
    }

    private fun assertOutput(expected: String) {
        assertThat(out.toString())
            .isEqualTo(expected)
    }

}
