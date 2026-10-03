package dev.sku20.stopgap.codegen

import dev.sku20.stopgap.codegen.ast.AstAssignment
import dev.sku20.stopgap.codegen.ast.AstCall
import dev.sku20.stopgap.codegen.ast.AstFile
import dev.sku20.stopgap.codegen.ast.AstFunction
import dev.sku20.stopgap.codegen.ast.AstLambda
import dev.sku20.stopgap.codegen.ast.AstLiteral
import dev.sku20.stopgap.codegen.ast.AstParam
import dev.sku20.stopgap.codegen.ast.AstReturn
import dev.sku20.stopgap.codegen.ast.AstStringLiteral
import dev.sku20.stopgap.codegen.ast.AstType
import dev.sku20.stopgap.codegen.ast.VariableType
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

        assertOutput("package dev.sku20.example\n")
    }

    @Test
    fun astFileContent() {
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
            """.trimIndent() + "\n"
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

        assertOutput("String")
    }

    @Test
    fun astTypeAlias() {
        val type = AstType("Builder", "io.helidon.webserver.http.HttpRouting.Builder", "RoutingBuilder")
        printer.visitType(type)

        assertOutput("RoutingBuilder")
    }

    @Test
    fun astParam() {
        val param = AstParam(AstLiteral("a"))
        printer.visitParam(param)

        assertOutput("a")
    }

    @Test
    fun astParamTyped() {
        val param = AstParam(AstLiteral("a"), AstType("String", "kotlin.String"))
        printer.visitParam(param)

        assertOutput("a: String")
    }

    @Test
    fun astCall() {
        val call = AstCall(AstLiteral("f"), listOf(AstLiteral("a"), AstLiteral("b")))
        printer.visitCall(call)

        assertOutput("f(a, b)")
    }

    @Test
    fun astCallReceiver() {
        val call = AstCall(AstLiteral("f"), listOf(AstLiteral("a")), AstLiteral("r"))
        printer.visitCall(call)

        assertOutput("r.f(a)")
    }

    @Test
    fun astReturn() {
        val ret = AstReturn()
        printer.visitReturn(ret)

        assertOutput("return")
    }

    @Test
    fun astReturnValue() {
        val ret = AstReturn(AstLiteral("a"))
        printer.visitReturn(ret)

        assertOutput("return a")
    }

    @Test
    fun astReturnLabel() {
        val ret = AstReturn(label = AstLiteral("l"))
        printer.visitReturn(ret)

        assertOutput("return@l")
    }

    @Test
    fun astReturnLabelValue() {
        val ret = AstReturn(AstLiteral("a"), AstLiteral("l"))
        printer.visitReturn(ret)

        assertOutput("return@l a")
    }

    @Test
    fun astAssignmentVal() {
        val assignment = AstAssignment(AstLiteral("a"), VariableType.VAL, AstLiteral("1"))
        printer.visitAssignment(assignment)

        assertOutput("val a = 1")
    }

    @Test
    fun astAssignmentVar() {
        val assignment = AstAssignment(AstLiteral("a"), VariableType.VAR, AstLiteral("1"))
        printer.visitAssignment(assignment)

        assertOutput("var a = 1")
    }

    @Test
    fun astAssignmentLateinit() {
        val assignment = AstAssignment(AstLiteral("a"), VariableType.LATEINIT_VAR, AstType("String", "kotlin.String"))
        printer.visitAssignment(assignment)

        assertOutput("lateinit var a: String")
    }

    @Test
    fun astAssignmentEmpty() {
        val assignment = AstAssignment(AstLiteral("a"), VariableType.EMPTY, AstLiteral("1"))
        printer.visitAssignment(assignment)

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
            fun f(): String {
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
