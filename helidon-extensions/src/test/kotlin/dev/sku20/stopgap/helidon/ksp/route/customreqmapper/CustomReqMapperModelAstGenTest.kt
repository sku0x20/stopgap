package dev.sku20.stopgap.helidon.ksp.route.customreqmapper

import dev.sku20.stopgap.codegen.ast.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CustomReqMapperModelAstGenTest {

    @Test
    fun qualifier() {
        val gen = CustomReqMapperModelAstGen(CustomReqMapperModel(null, "a.xml"))

        assertThat(gen.name()).isEqualTo(AstLiteral("a_xml"))
        assertThat(gen.param()).isEqualTo(qualifierParam("a_xml", "a.xml"))
    }

    @Test
    fun type() {
        val gen = CustomReqMapperModelAstGen(CustomReqMapperModel("a.Mapper", null))

        assertThat(gen.name()).isEqualTo(AstLiteral("a_Mapper"))
        assertThat(gen.param()).isEqualTo(AstParam(AstLiteral("a_Mapper"), AstType("a.Mapper")))
    }

    @Test
    fun qualifierOverType() {
        val gen = CustomReqMapperModelAstGen(CustomReqMapperModel("a.Mapper", "a.xml"))

        assertThat(gen.param()).isEqualTo(qualifierParam("a_xml", "a.xml"))
    }

    @Test
    fun defaultQualifier() {
        val gen = CustomReqMapperModelAstGen(null)

        assertThat(gen.param()).isEqualTo(
            qualifierParam(
                "dev_sku20_stopgap_helidon_mapper_req_default",
                "dev.sku20.stopgap.helidon.mapper.req.default"
            )
        )
    }

    private fun qualifierParam(name: String, qualifier: String) = AstParam(
        AstLiteral(name),
        AstType("dev.sku20.stopgap.helidon.mapper.req.ReqMapper"),
        listOf(
            AstAnnotation(
                AstLiteral("dev.sku20.stopgap.helidon.ksp.registry.RegistryQualifier"),
                listOf(AstStringLiteral(qualifier))
            )
        )
    )
}
