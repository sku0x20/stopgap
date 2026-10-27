package dev.sku20.stopgap.helidon.mapper.req.multipart

import dev.sku20.stopgap.helidon.mapper.req.exception.InvalidBoundaryException
import dev.sku20.stopgap.helidon.mapper.req.exception.MissingBoundaryException
import io.helidon.http.HttpMediaType
import java.io.InputStream

class MultipartRequest(
    private val mediaType: HttpMediaType,
    private val bodyStream: InputStream
) {

    private lateinit var boundary: String
    private val parts = mutableListOf<Part>()

    fun multiPart(): MultiPart {
        parseBoundary()
        parseParts()
        return MultiPart(emptyList())
    }

    private fun parseParts() {
//        val part = PartParser(bodyStream)
//            .part()
//        parts.add(part)
    }

    private fun parseBoundary() {
        val value = mediaType.parameters()["boundary"]
        if (value.isNullOrEmpty()) throw MissingBoundaryException()
        if (!isValidBoundary(value)) throw InvalidBoundaryException()
        boundary = value
    }

    // RFC 2046: 1 to 70 of the allowed characters, not ending in a space.
    private fun isValidBoundary(value: String): Boolean {
        if (value.length > MAX_BOUNDARY_LENGTH) return false
        if (value.endsWith(' ')) return false
        for (char in value) {
            if (!isBoundaryChar(char)) return false
        }
        return true
    }

    private fun isBoundaryChar(char: Char): Boolean =
        char in 'a'..'z' || char in 'A'..'Z' || char in '0'..'9' || char in BOUNDARY_SYMBOLS

    private companion object {
        const val MAX_BOUNDARY_LENGTH = 70
        const val BOUNDARY_SYMBOLS = "'()+_,-./:=? "
    }
}