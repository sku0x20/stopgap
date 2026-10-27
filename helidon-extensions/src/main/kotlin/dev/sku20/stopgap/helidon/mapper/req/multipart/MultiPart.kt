package dev.sku20.stopgap.helidon.mapper.req.multipart

/**
 * Parts are handed to the endpoint undecoded. Decoding each part by its content type would bring back the
 * media type → decoder mapping that mappers exist to avoid, so the endpoint decides what each part means.
 */
data class MultiPart(
    val parts: List<Part>,
)
