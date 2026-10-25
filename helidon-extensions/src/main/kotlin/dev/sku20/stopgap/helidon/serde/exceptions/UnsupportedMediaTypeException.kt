package dev.sku20.stopgap.helidon.serde.exceptions

import io.helidon.http.HttpException
import io.helidon.http.HttpMediaType
import io.helidon.http.Status

// todo:
class UnsupportedMediaTypeException(mediaType: HttpMediaType?) :
    HttpException(messageFor(mediaType), Status.UNSUPPORTED_MEDIA_TYPE_415)

private fun messageFor(mediaType: HttpMediaType?): String =
    if (mediaType == null) "Missing Content-Type" else "Unsupported Content-Type: ${mediaType.text()}"
