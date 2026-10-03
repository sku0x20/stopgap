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
        printer.visitType(AstType("kotlin.String"))
        assertOutput("kotlin.String")

        out.reset()

        printer.visitType(AstType("kotlin.String", emptyList(), true))
        assertOutput("kotlin.String?")

        out.reset()

        printer.visitType(
            AstType(
                "kotlin.collections.Map",
                listOf(
                    AstType("kotlin.String"),
                    AstType(
                        "kotlin.collections.List",
                        listOf(AstType("kotlin.Int", emptyList(), true))
                    )
                ),
                true
            )
        )
        assertOutput("kotlin.collections.Map<kotlin.String, kotlin.collections.List<kotlin.Int?>>?")
    }

    @Test
    fun astParam() {
        printer.visitParam(AstParam(AstLiteral("a")))
        assertOutput("a")

        out.reset()

        printer.visitParam(
            AstParam(
                AstLiteral("a"),
                AstType("kotlin.String")
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

        out.reset()

        printer.visitCall(
            AstCall(
                AstLiteral("f"),
                listOf(AstLiteral("a")),
                AstLiteral("r"),
                listOf(
                    AstType("kotlin.String"),
                    AstType("kotlin.Int")
                )
            )
        )
        assertOutput("r.f<kotlin.String, kotlin.Int>(a)")
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
                AstType("kotlin.String"),
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
        printer.visitFunction(
            AstFunction(
                "f",
                listOf(
                    AstParam(AstLiteral("a")),
                    AstParam(AstLiteral("b"))
                ),
                listOf(
                    AstLiteral("x"),
                    AstLiteral("y")
                )
            )
        )
        assertOutput(
            """
            fun f(a, b) {
            x
            y
            }
            """.trimIndent()
        )

        out.reset()

        printer.visitFunction(
            AstFunction(
                "f",
                emptyList(),
                listOf(AstLiteral("x")),
                AstType("kotlin.String")
            )
        )
        assertOutput(
            """
            fun f(): kotlin.String {
            x
            }
            """.trimIndent()
        )

        out.reset()

        printer.visitFunction(
            AstFunction(
                "f",
                listOf(AstParam(AstLiteral("a"), AstLiteral("T"))),
                listOf(AstLiteral("x")),
                null,
                listOf(AstLiteral("T"))
            )
        )
        assertOutput(
            """
            fun <T> f(a: T) {
            x
            }
            """.trimIndent()
        )
    }

    @Test
    fun astLambda() {
        printer.visitLambda(
            AstLambda(
                listOf(
                    AstParam(AstLiteral("a")),
                    AstParam(AstLiteral("b"))
                ),
                listOf(
                    AstLiteral("x"),
                    AstLiteral("y")
                )
            )
        )
        assertOutput(
            """
            { a, b ->
            x
            y
            }
            """.trimIndent()
        )

        out.reset()

        printer.visitLambda(
            AstLambda(
                emptyList(),
                listOf(AstLiteral("x"))
            )
        )
        assertOutput(
            """
            {
            x
            }
            """.trimIndent()
        )
    }

    private fun assertOutput(expected: String) {
        assertThat(out.toString())
            .isEqualTo(expected)
    }

}
