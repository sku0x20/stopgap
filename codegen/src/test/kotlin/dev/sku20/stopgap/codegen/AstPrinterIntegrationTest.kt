package dev.sku20.stopgap.codegen

import dev.sku20.stopgap.codegen.ast.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class AstPrinterIntegrationTest {

    @Test
    fun printFile() {
        val file = AstFile(
            AstLiteral("dev.sku20.example"),
            listOf(
                AstFunction(
                    "greet",
                    listOf(AstParam(AstLiteral("names"), AstType("kotlin.collections.List"))),
                    listOf(
                        AstAssignment(
                            AstLiteral("greeting"),
                            variableType = VariableType.VAL,
                            value = AstStringLiteral("Hello")
                        ),
                        AstCall(
                            AstLiteral("forEach"),
                            listOf(
                                AstLambda(
                                    listOf(AstParam(AstLiteral("name"))),
                                    listOf(
                                        AstCall(
                                            AstLiteral("println"),
                                            listOf(AstLiteral("greeting + name"))
                                        )
                                    )
                                )
                            ),
                            AstLiteral("names")
                        ),
                        AstReturn(AstLiteral("greeting"))
                    ),
                    AstType("kotlin.String")
                )
            )
        )

        val out = ByteArrayOutputStream()
        AstPrinter(out).visitFile(file)

        assertThat(out.toString()).isEqualTo(
            """
            package dev.sku20.example

            fun greet(names: kotlin.collections.List): kotlin.String {
            val greeting = "Hello"
            names.forEach({ name ->
            println(greeting + name)
            })
            return greeting
            }

            """.trimIndent()
        )
    }

}
