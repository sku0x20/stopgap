package dev.sku20.stopgap.helidon.mapper.bodyreqmapper.json

import io.helidon.http.HttpMediaType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JsonMediaTypeRuleTest {

    @Test
    fun acceptsJson() {
        assertThat(JsonMediaTypeRule.accepts(HttpMediaType.create("application/json"))).isTrue()
    }

    @Test
    fun acceptsJsonWithParameters() {
        assertThat(JsonMediaTypeRule.accepts(HttpMediaType.create("application/json; charset=utf-8"))).isTrue()
    }

    @Test
    fun ignoresCase() {
        assertThat(JsonMediaTypeRule.accepts(HttpMediaType.create("Application/JSON"))).isTrue()
    }

    @Test
    fun acceptsVendorJson() {
        assertThat(JsonMediaTypeRule.accepts(HttpMediaType.create("application/vnd.acme+json"))).isTrue()
    }

    @Test
    fun rejectsPlainText() {
        assertThat(JsonMediaTypeRule.accepts(HttpMediaType.create("text/plain"))).isFalse()
    }

    @Test
    fun rejectsMissingMediaType() {
        assertThat(JsonMediaTypeRule.accepts(null)).isFalse()
    }

    @Test
    fun rejectsWildcards() {
        assertThat(JsonMediaTypeRule.accepts(HttpMediaType.create("*/*"))).isFalse()
        assertThat(JsonMediaTypeRule.accepts(HttpMediaType.create("application/*"))).isFalse()
    }
}
