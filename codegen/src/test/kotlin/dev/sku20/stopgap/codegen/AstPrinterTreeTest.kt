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

class AstPrinterTreeTest {

    private val out = ByteArrayOutputStream()
    private val printer = AstPrinter(out)

    @Test
    fun fullTree() {
        val stringType = AstType("String", "kotlin.String")
        val routerType = AstType("Builder", "io.helidon.webserver.http.HttpRouting.Builder", "RoutingBuilder")

        val greet = AstFunction(
            "greet",
            listOf(AstParam(AstLiteral("name"), stringType)),
            listOf(AstReturn(AstStringLiteral("hello"))),
            stringType
        )

        val register = AstFunction(
            "register",
            listOf(
                AstParam(AstLiteral("router"), routerType),
                AstParam(AstLiteral("registry"), AstType("Registry", "dev.sku20.stopgap.ir.Registry")),
            ),
            listOf(
                AstAssignment(
                    AstLiteral("service"),
                    VariableType.VAL,
                    AstCall(AstLiteral("get"), emptyList(), AstLiteral("registry"))
                ),
                AstAssignment(AstLiteral("count"), VariableType.VAR, AstLiteral("0")),
                AstAssignment(AstLiteral("label"), VariableType.LATEINIT_VAR, stringType),
                AstAssignment(AstLiteral("count"), VariableType.EMPTY, AstLiteral("1")),
                AstCall(
                    AstLiteral("get"),
                    listOf(
                        AstStringLiteral("/hello"),
                        AstLambda(
                            listOf(AstParam(AstLiteral("req")), AstParam(AstLiteral("res"))),
                            listOf(
                                AstCall(
                                    AstLiteral("send"),
                                    listOf(AstCall(AstLiteral("greet"), listOf(AstStringLiteral("world")))),
                                    AstLiteral("res")
                                ),
                                AstReturn(label = AstLiteral("get")),
                            )
                        ),
                    ),
                    AstLiteral("router")
                ),
                AstReturn(),
            )
        )

        val file = AstFile(
            AstLiteral("dev.sku20.example"),
            listOf(greet, register)
        )
        printer.visitFile(file)

        assertThat(out.toString())
            .isEqualTo(
                """
                package dev.sku20.example

                fun greet(name: String): String {
                    return "hello"
                }

                fun register(router: RoutingBuilder, registry: Registry) {
                    val service = registry.get()
                    var count = 0
                    lateinit var label: String
                    count = 1
                    router.get("/hello", { req, res ->
                        res.send(greet("world"))
                        return@get
                    })
                    return
                }
                """.trimIndent() + "\n"
            )
    }

}
