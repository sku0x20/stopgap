package dev.sku20.stopgap.helidon.mapper.json

import io.helidon.http.HttpMediaType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JsonContentTypeRuleTest {

    @Test
    fun acceptsJson() {
        assertThat(JsonContentTypeRule.accepts(HttpMediaType.create("application/json"))).isTrue()
    }

    @Test
    fun acceptsJsonWithParameters() {
        assertThat(JsonContentTypeRule.accepts(HttpMediaType.create("application/json; charset=utf-8"))).isTrue()
    }

    @Test
    fun ignoresCase() {
        assertThat(JsonContentTypeRule.accepts(HttpMediaType.create("Application/JSON"))).isTrue()
    }

    @Test
    fun acceptsVendorJson() {
        assertThat(JsonContentTypeRule.accepts(HttpMediaType.create("application/vnd.acme+json"))).isTrue()
    }

    @Test
    fun rejectsPlainText() {
        assertThat(JsonContentTypeRule.accepts(HttpMediaType.create("text/plain"))).isFalse()
    }

    @Test
    fun rejectsMissingContentType() {
        assertThat(JsonContentTypeRule.accepts(null)).isFalse()
    }

    @Test
    fun rejectsWildcards() {
        assertThat(JsonContentTypeRule.accepts(HttpMediaType.create("*/*"))).isFalse()
        assertThat(JsonContentTypeRule.accepts(HttpMediaType.create("application/*"))).isFalse()
    }
}
