package dev.sku20.stopgap.helidon.mapper.req.multipart

/**
 * Parts are handed to the endpoint undecoded. Decoding each part by its content type would bring back the
 * media type → decoder mapping that mappers exist to avoid, so the endpoint decides what each part means.
 *
 * Generated routes [close] it once the response is sent, deleting the temp files behind large parts, so a part's
 * stream must not outlive the request (e.g. handed to another thread).
 */
data class MultiPart(
    val parts: List<Part>,
) : AutoCloseable {

    override fun close() {
        for (part in parts) {
            part.close()
        }
    }
}
