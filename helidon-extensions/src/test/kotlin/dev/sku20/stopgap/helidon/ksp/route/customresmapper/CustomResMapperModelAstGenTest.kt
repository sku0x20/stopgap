package dev.sku20.stopgap.helidon.ksp.route.customresmapper

import dev.sku20.stopgap.codegen.ast.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CustomResMapperModelAstGenTest {

    @Test
    fun qualifier() {
        val gen = CustomResMapperModelAstGen(CustomResMapperModel(null, "a.xml"))

        assertThat(gen.name()).isEqualTo(AstLiteral("a_xml"))
        assertThat(gen.param()).isEqualTo(qualifierParam("a_xml", "a.xml"))
    }

    @Test
    fun type() {
        val gen = CustomResMapperModelAstGen(CustomResMapperModel("a.Mapper", null))

        assertThat(gen.name()).isEqualTo(AstLiteral("a_Mapper"))
        assertThat(gen.param()).isEqualTo(AstParam(AstLiteral("a_Mapper"), AstType("a.Mapper")))
    }

    @Test
    fun qualifierOverType() {
        val gen = CustomResMapperModelAstGen(CustomResMapperModel("a.Mapper", "a.xml"))

        assertThat(gen.param()).isEqualTo(qualifierParam("a_xml", "a.xml"))
    }

    @Test
    fun defaultQualifier() {
        val gen = CustomResMapperModelAstGen(null)

        assertThat(gen.param()).isEqualTo(
            qualifierParam(
                "dev_sku20_stopgap_helidon_mapper_res_default",
                "dev.sku20.stopgap.helidon.mapper.res.default"
            )
        )
    }

    private fun qualifierParam(name: String, qualifier: String) = AstParam(
        AstLiteral(name),
        AstType("dev.sku20.stopgap.helidon.mapper.res.ResMapper"),
        listOf(
            AstAnnotation(
                AstLiteral("dev.sku20.stopgap.helidon.ksp.registry.RegistryQualifier"),
                listOf(AstStringLiteral(qualifier))
            )
        )
    )
}
