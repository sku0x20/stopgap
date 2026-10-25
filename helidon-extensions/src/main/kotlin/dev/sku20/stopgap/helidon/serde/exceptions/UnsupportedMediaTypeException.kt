package dev.sku20.stopgap.helidon.serde.exceptions

import io.helidon.http.HttpException
import io.helidon.http.HttpMediaType
import io.helidon.http.Status

// todo:
class UnsupportedMediaTypeException(mediaType: HttpMediaType) :
    HttpException("Unsupported Content-Type: ${mediaType.text()}", Status.UNSUPPORTED_MEDIA_TYPE_415)
