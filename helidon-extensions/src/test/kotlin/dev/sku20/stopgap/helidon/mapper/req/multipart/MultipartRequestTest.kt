package dev.sku20.stopgap.helidon.mapper.req.multipart

import dev.sku20.stopgap.helidon.mapper.req.exception.InvalidBoundaryException
import dev.sku20.stopgap.helidon.mapper.req.exception.MissingBoundaryException
import io.helidon.http.HttpMediaType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

class MultipartRequestTest {

    @Test
    fun validatesBoundary() {
        assertThatThrownBy { multiPart("multipart/form-data") }
            .isInstanceOf(MissingBoundaryException::class.java)
        assertThatThrownBy { multiPart("multipart/form-data; boundary=\"\"") }
            .isInstanceOf(MissingBoundaryException::class.java)
        assertThatThrownBy { multiPart("multipart/form-data; boundary=${"a".repeat(71)}") }
            .isInstanceOf(InvalidBoundaryException::class.java)
        assertThatThrownBy { multiPart("multipart/form-data; boundary=\"ab@c\"") }
            .isInstanceOf(InvalidBoundaryException::class.java)
        assertThatThrownBy { multiPart("multipart/form-data; boundary=\"abc \"") }
            .isInstanceOf(InvalidBoundaryException::class.java)

        assertThatCode { multiPart("multipart/form-data; boundary=${"a".repeat(70)}", "--${"a".repeat(70)}--") }
            .doesNotThrowAnyException()
        assertThatCode { multiPart("multipart/form-data; boundary=\"abc\"", "--abc--") }
            .doesNotThrowAnyException()
    }

    @Test
    fun parsesValidRequest() {
        val multiPart = multiPart("multipart/form-data; boundary=abc", "--abc--")

        assertThat(multiPart.parts).isEmpty()
    }

    private fun multiPart(contentType: String, body: String = ""): MultiPart =
        MultipartRequest(HttpMediaType.create(contentType), ByteArrayInputStream(body.toByteArray())).multiPart()
}
