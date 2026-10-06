package dev.sku20.stopgap.helidon.ksp.route

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getDeclaredFunctions
import dev.sku20.stopgap.helidon.ksp.KspRunner
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CustomSerdeCatalogModelParserTest {

    @Test
    fun noAnnotation() {
        assertThat(parse("class A")).isNull()
    }

    @Test
    fun defaults() {
        assertThat(parse("@CustomSerdeCatalog class A"))
            .isEqualTo(CustomSerdeCatalogModel(null, null))
    }

    @Test
    fun qualifier() {
        assertThat(parse("@CustomSerdeCatalog(qualifier = \"q\") class A"))
            .isEqualTo(CustomSerdeCatalogModel(null, "q"))
    }

    @Test
    fun clazz() {
        assertThat(parse("@CustomSerdeCatalog(clazz = B::class) class A\nclass B"))
            .isEqualTo(CustomSerdeCatalogModel("a.B", null))
    }

    @Test
    fun onFunction() {
        assertThat(parse("class A { @CustomSerdeCatalog(qualifier = \"q\") fun f() {} }", "f"))
            .isEqualTo(CustomSerdeCatalogModel(null, "q"))
    }

    private fun parse(code: String, function: String? = null): CustomSerdeCatalogModel? {
        val source = "package a\nimport dev.sku20.stopgap.helidon.serde.CustomSerdeCatalog\n$code"
        return KspRunner(source) { resolver ->
            val clazz = resolver.getClassDeclarationByName("a.A")!!
            if (function == null) {
                CustomSerdeCatalogModelParser(clazz).parse()
            } else {
                CustomSerdeCatalogModelParser(clazz.getDeclaredFunctions().first { it.simpleName.asString() == function }).parse()
            }
        }.run()
    }
}
