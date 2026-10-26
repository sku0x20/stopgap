package dev.sku20.stopgap.helidon.mapper.plaintext

import io.helidon.http.HttpMediaType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PlainTextMediaTypeRuleTest {

    @Test
    fun acceptsPlainTextWithoutCharset() {
        assertThat(PlainTextMediaTypeRule.accepts(HttpMediaType.create("text/plain"))).isTrue()
    }

    @Test
    fun acceptsPlainTextUtf8() {
        assertThat(PlainTextMediaTypeRule.accepts(HttpMediaType.create("text/plain; charset=utf-8"))).isTrue()
    }

    @Test
    fun ignoresCase() {
        assertThat(PlainTextMediaTypeRule.accepts(HttpMediaType.create("Text/Plain; charset=UTF-8"))).isTrue()
    }

    @Test
    fun rejectsOtherCharset() {
        assertThat(PlainTextMediaTypeRule.accepts(HttpMediaType.create("text/plain; charset=ISO-8859-1"))).isFalse()
    }

    @Test
    fun rejectsOtherTypes() {
        assertThat(PlainTextMediaTypeRule.accepts(HttpMediaType.create("text/html"))).isFalse()
        assertThat(PlainTextMediaTypeRule.accepts(HttpMediaType.create("application/json"))).isFalse()
    }

    @Test
    fun rejectsMissingMediaType() {
        assertThat(PlainTextMediaTypeRule.accepts(null)).isFalse()
    }

    @Test
    fun rejectsWildcards() {
        assertThat(PlainTextMediaTypeRule.accepts(HttpMediaType.create("*/*"))).isFalse()
        assertThat(PlainTextMediaTypeRule.accepts(HttpMediaType.create("text/*"))).isFalse()
    }
}
