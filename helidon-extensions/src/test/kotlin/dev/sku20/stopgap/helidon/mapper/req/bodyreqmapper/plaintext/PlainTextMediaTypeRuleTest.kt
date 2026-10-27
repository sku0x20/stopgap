package dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.plaintext

import io.helidon.http.HttpMediaType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PlainTextMediaTypeRuleTest {

    private val rule = PlainTextMediaTypeRule()

    @Test
    fun acceptsPlainTextWithoutCharset() {
        assertThat(rule.accepts(HttpMediaType.create("text/plain"))).isTrue()
    }

    @Test
    fun acceptsPlainTextUtf8() {
        assertThat(rule.accepts(HttpMediaType.create("text/plain; charset=utf-8"))).isTrue()
    }

    @Test
    fun ignoresCase() {
        assertThat(rule.accepts(HttpMediaType.create("Text/Plain; charset=UTF-8"))).isTrue()
    }

    @Test
    fun rejectsOtherCharset() {
        assertThat(rule.accepts(HttpMediaType.create("text/plain; charset=ISO-8859-1"))).isFalse()
    }

    @Test
    fun rejectsOtherTypes() {
        assertThat(rule.accepts(HttpMediaType.create("text/html"))).isFalse()
        assertThat(rule.accepts(HttpMediaType.create("application/json"))).isFalse()
    }

    @Test
    fun rejectsMissingMediaType() {
        assertThat(rule.accepts(null)).isFalse()
    }

    @Test
    fun rejectsWildcards() {
        assertThat(rule.accepts(HttpMediaType.create("*/*"))).isFalse()
        assertThat(rule.accepts(HttpMediaType.create("text/*"))).isFalse()
    }
}
