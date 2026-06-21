package dev.sku20.helidon.serde

import io.helidon.http.HttpException
import io.helidon.http.HttpMediaType
import io.helidon.http.Status

class UnsupportedMediaTypeException(mediaType: HttpMediaType) :
    HttpException("No serde available for $mediaType", Status.UNSUPPORTED_MEDIA_TYPE_415)
