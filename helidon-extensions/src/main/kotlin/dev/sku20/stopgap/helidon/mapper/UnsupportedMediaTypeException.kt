package dev.sku20.stopgap.helidon.mapper

import io.helidon.http.HttpException
import io.helidon.http.HttpMediaType
import io.helidon.http.Status

class UnsupportedMediaTypeException(contentType: HttpMediaType?) :
    HttpException("Unsupported content type: $contentType", Status.UNSUPPORTED_MEDIA_TYPE_415)
