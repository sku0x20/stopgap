package dev.sku20.stopgap.helidon.ksp.route.customserdecatalog

import dev.sku20.stopgap.codegen.ast.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CustomSerdeCatalogModelAstGenTest {

    @Test
    fun qualifier() {
        val gen = CustomSerdeCatalogModelAstGen(CustomSerdeCatalogModel(null, "a.xml"))

        assertThat(gen.name()).isEqualTo(AstLiteral("a_xml"))
        assertThat(gen.param()).isEqualTo(qualifierParam("a_xml", "a.xml"))
    }

    @Test
    fun type() {
        val gen = CustomSerdeCatalogModelAstGen(CustomSerdeCatalogModel("a.Catalog", null))

        assertThat(gen.name()).isEqualTo(AstLiteral("a_Catalog"))
        assertThat(gen.param()).isEqualTo(AstParam(AstLiteral("a_Catalog"), AstType("a.Catalog")))
    }

    @Test
    fun qualifierOverType() {
        val gen = CustomSerdeCatalogModelAstGen(CustomSerdeCatalogModel("a.Catalog", "a.xml"))

        assertThat(gen.param()).isEqualTo(qualifierParam("a_xml", "a.xml"))
    }

    @Test
    fun defaultQualifier() {
        val gen = CustomSerdeCatalogModelAstGen(null)

        assertThat(gen.param()).isEqualTo(
            qualifierParam(
                "dev_sku20_stopgap_helidon_serde_catalog_default",
                "dev.sku20.stopgap.helidon.serde.catalog.default"
            )
        )
    }

    private fun qualifierParam(name: String, qualifier: String) = AstParam(
        AstLiteral(name),
        AstType("dev.sku20.stopgap.helidon.serde.SerdeCatalog"),
        listOf(
            AstAnnotation(
                AstLiteral("dev.sku20.stopgap.helidon.ksp.registry.RegistryQualifier"),
                listOf(AstStringLiteral(qualifier))
            )
        )
    )
}
