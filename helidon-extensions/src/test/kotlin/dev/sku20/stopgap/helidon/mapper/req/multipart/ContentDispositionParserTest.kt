package dev.sku20.stopgap.helidon.mapper.req.multipart

import dev.sku20.stopgap.helidon.mapper.req.exception.MalformedBodyException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ContentDispositionParserTest {

    @Test
    fun parsesNameAndFileName() {
        assertThat(parse("form-data; name=\"attachment\"; filename=\"notes.txt\""))
            .isEqualTo(mapOf("name" to "attachment", "filename" to "notes.txt"))
    }

    @Test
    fun parsesEmptyFileName() {
        assertThat(parse("form-data; name=\"attachment\"; filename=\"\""))
            .isEqualTo(mapOf("name" to "attachment", "filename" to ""))
    }

    @Test
    fun parsesUnquotedValue() {
        assertThat(parse("form-data; name=title")).isEqualTo(mapOf("name" to "title"))
    }

    @Test
    fun ignoresParameterNameCase() {
        assertThat(parse("form-data; NAME=\"title\"; FileName=\"a.txt\""))
            .isEqualTo(mapOf("name" to "title", "filename" to "a.txt"))
    }

    @Test
    fun keepsSemicolonInQuotedValue() {
        assertThat(parse("form-data; name=\"a;b\"")).isEqualTo(mapOf("name" to "a;b"))
    }

    @Test
    fun unescapesQuoteAndBackslash() {
        assertThat(parse("form-data; name=\"say \\\"hi\\\" \\\\ bye\""))
            .isEqualTo(mapOf("name" to "say \"hi\" \\ bye"))
    }

    @Test
    fun keepsWindowsPathBackslashes() {
        assertThat(parse("form-data; name=\"f\"; filename=\"C:\\dir\\a.txt\""))
            .isEqualTo(mapOf("name" to "f", "filename" to "C:\\dir\\a.txt"))
    }

    @Test
    fun keepsFirstOfRepeatedName() {
        assertThat(parse("form-data; name=\"a\"; name=\"b\"")).isEqualTo(mapOf("name" to "a"))
    }

    @Test
    fun allowsTrailingSemicolon() {
        assertThat(parse("form-data; name=\"a\";")).isEqualTo(mapOf("name" to "a"))
    }

    @Test
    fun parsesTypeOnly() {
        assertThat(parse("form-data")).isEmpty()
    }

    @Test
    fun rejectsUnterminatedQuote() {
        assertThatThrownBy { parse("form-data; name=\"a") }.isInstanceOf(MalformedBodyException::class.java)
    }

    @Test
    fun rejectsParameterWithoutEquals() {
        assertThatThrownBy { parse("form-data; name") }.isInstanceOf(MalformedBodyException::class.java)
    }

    @Test
    fun rejectsTextAfterQuotedValue() {
        assertThatThrownBy { parse("form-data; name=\"a\"b") }.isInstanceOf(MalformedBodyException::class.java)
    }

    private fun parse(value: String) = ContentDispositionParser(value).parse()
}
