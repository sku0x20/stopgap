package dev.sku20.stopgap.helidon.mapper.req.multipart

import dev.sku20.stopgap.helidon.mapper.req.exception.InvalidBoundaryException
import dev.sku20.stopgap.helidon.mapper.req.exception.MissingBoundaryException
import io.helidon.http.HttpMediaType
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

class MultipartRequestTest {

    @Test
    fun rejectsMissingBoundary() {
        assertThatThrownBy { multiPart("multipart/form-data") }
            .isInstanceOf(MissingBoundaryException::class.java)
    }

    @Test
    fun rejectsEmptyBoundary() {
        assertThatThrownBy { multiPart("multipart/form-data; boundary=\"\"") }
            .isInstanceOf(MissingBoundaryException::class.java)
    }

    @Test
    fun rejectsTooLongBoundary() {
        assertThatThrownBy { multiPart("multipart/form-data; boundary=${"a".repeat(71)}") }
            .isInstanceOf(InvalidBoundaryException::class.java)
    }

    @Test
    fun acceptsMaxLengthBoundary() {
        assertThatCode { multiPart("multipart/form-data; boundary=${"a".repeat(70)}") }
            .doesNotThrowAnyException()
    }

    @Test
    fun rejectsInvalidBoundaryChar() {
        assertThatThrownBy { multiPart("multipart/form-data; boundary=\"ab@c\"") }
            .isInstanceOf(InvalidBoundaryException::class.java)
    }

    @Test
    fun rejectsBoundaryEndingInSpace() {
        assertThatThrownBy { multiPart("multipart/form-data; boundary=\"abc \"") }
            .isInstanceOf(InvalidBoundaryException::class.java)
    }

    @Test
    fun acceptsQuotedBoundary() {
        assertThatCode { multiPart("multipart/form-data; boundary=\"abc\"") }
            .doesNotThrowAnyException()
    }

    private fun multiPart(contentType: String): MultiPart =
        MultipartRequest(HttpMediaType.create(contentType), ByteArrayInputStream(ByteArray(0))).multiPart()
}
