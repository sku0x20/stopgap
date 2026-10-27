package dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.json

import io.helidon.http.HttpMediaType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JsonMediaTypeRuleTest {

    private val rule = JsonMediaTypeRule()

    @Test
    fun acceptsJson() {
        assertThat(rule.accepts(HttpMediaType.create("application/json"))).isTrue()
    }

    @Test
    fun acceptsJsonWithParameters() {
        assertThat(rule.accepts(HttpMediaType.create("application/json; charset=utf-8"))).isTrue()
    }

    @Test
    fun ignoresCase() {
        assertThat(rule.accepts(HttpMediaType.create("Application/JSON"))).isTrue()
    }

    @Test
    fun acceptsVendorJson() {
        assertThat(rule.accepts(HttpMediaType.create("application/vnd.acme+json"))).isTrue()
    }

    @Test
    fun rejectsPlainText() {
        assertThat(rule.accepts(HttpMediaType.create("text/plain"))).isFalse()
    }

    @Test
    fun rejectsMissingMediaType() {
        assertThat(rule.accepts(null)).isFalse()
    }

    @Test
    fun rejectsWildcards() {
        assertThat(rule.accepts(HttpMediaType.create("*/*"))).isFalse()
        assertThat(rule.accepts(HttpMediaType.create("application/*"))).isFalse()
    }
}
