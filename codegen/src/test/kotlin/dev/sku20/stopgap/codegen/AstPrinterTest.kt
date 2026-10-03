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

        assertThat(out.toString())
            .isEqualTo("package dev.sku20.example\n")
    }

    @Test
    fun astFileContent() {
        val file = AstFile(
            AstLiteral("dev.sku20.example"),
            listOf(AstLiteral("a"), AstLiteral("b"))
        )
        printer.visitFile(file)

        assertThat(out.toString())
            .isEqualTo(
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

        assertThat(out.toString())
            .isEqualTo("42")
    }

    @Test
    fun astStringLiteral() {
        val literal = AstStringLiteral("hello")
        printer.visitStringLiteral(literal)

        assertThat(out.toString())
            .isEqualTo("\"hello\"")
    }

    @Test
    fun astType() {
        val type = AstType("String", "kotlin.String")
        printer.visitType(type)

        assertThat(out.toString())
            .isEqualTo("String")
    }

    @Test
    fun astTypeAlias() {
        val type = AstType("Builder", "io.helidon.webserver.http.HttpRouting.Builder", "RoutingBuilder")
        printer.visitType(type)

        assertThat(out.toString())
            .isEqualTo("RoutingBuilder")
    }

    @Test
    fun astParam() {
        val param = AstParam(AstLiteral("a"))
        printer.visitParam(param)

        assertThat(out.toString())
            .isEqualTo("a")
    }

    @Test
    fun astParamTyped() {
        val param = AstParam(AstLiteral("a"), AstType("String", "kotlin.String"))
        printer.visitParam(param)

        assertThat(out.toString())
            .isEqualTo("a: String")
    }

    @Test
    fun astCall() {
        val call = AstCall(AstLiteral("f"), listOf(AstLiteral("a"), AstLiteral("b")))
        printer.visitCall(call)

        assertThat(out.toString())
            .isEqualTo("f(a, b)")
    }

    @Test
    fun astCallReceiver() {
        val call = AstCall(AstLiteral("f"), listOf(AstLiteral("a")), AstLiteral("r"))
        printer.visitCall(call)

        assertThat(out.toString())
            .isEqualTo("r.f(a)")
    }

    @Test
    fun astReturn() {
        val ret = AstReturn()
        printer.visitReturn(ret)

        assertThat(out.toString())
            .isEqualTo("return")
    }

    @Test
    fun astReturnValue() {
        val ret = AstReturn(AstLiteral("a"))
        printer.visitReturn(ret)

        assertThat(out.toString())
            .isEqualTo("return a")
    }

    @Test
    fun astReturnLabel() {
        val ret = AstReturn(label = AstLiteral("l"))
        printer.visitReturn(ret)

        assertThat(out.toString())
            .isEqualTo("return@l")
    }

    @Test
    fun astReturnLabelValue() {
        val ret = AstReturn(AstLiteral("a"), AstLiteral("l"))
        printer.visitReturn(ret)

        assertThat(out.toString())
            .isEqualTo("return@l a")
    }

    @Test
    fun astAssignmentVal() {
        val assignment = AstAssignment(AstLiteral("a"), VariableType.VAL, AstLiteral("1"))
        printer.visitAssignment(assignment)

        assertThat(out.toString())
            .isEqualTo("val a = 1")
    }

    @Test
    fun astAssignmentVar() {
        val assignment = AstAssignment(AstLiteral("a"), VariableType.VAR, AstLiteral("1"))
        printer.visitAssignment(assignment)

        assertThat(out.toString())
            .isEqualTo("var a = 1")
    }

    @Test
    fun astAssignmentLateinit() {
        val assignment = AstAssignment(AstLiteral("a"), VariableType.LATEINIT_VAR, AstType("String", "kotlin.String"))
        printer.visitAssignment(assignment)

        assertThat(out.toString())
            .isEqualTo("lateinit var a: String")
    }

    @Test
    fun astAssignmentEmpty() {
        val assignment = AstAssignment(AstLiteral("a"), VariableType.EMPTY, AstLiteral("1"))
        printer.visitAssignment(assignment)

        assertThat(out.toString())
            .isEqualTo("a = 1")
    }

    @Test
    fun astFunction() {
        val function = AstFunction(
            "f",
            listOf(AstParam(AstLiteral("a")), AstParam(AstLiteral("b"))),
            listOf(AstLiteral("x"), AstLiteral("y"))
        )
        printer.visitFunction(function)

        assertThat(out.toString())
            .isEqualTo(
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

        assertThat(out.toString())
            .isEqualTo(
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

        assertThat(out.toString())
            .isEqualTo(
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

        assertThat(out.toString())
            .isEqualTo(
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

        assertThat(out.toString())
            .isEqualTo(
                """
                fun f() {
                    run({
                        x
                    })
                }
                """.trimIndent()
            )
    }

}
