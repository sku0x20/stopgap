package dev.sku20.stopgap.helidon.mapper.req.multipart

import dev.sku20.stopgap.helidon.mapper.req.multipart.part.Part

data class MultiPart(
    val parts: List<Part>,
)


