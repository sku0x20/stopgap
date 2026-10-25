package dev.sku20.stopgap.helidon.serde.exceptions

import io.helidon.http.HttpException
import io.helidon.http.Status

class MissingContentTypeException :
    HttpException("Missing Content-Type", Status.UNSUPPORTED_MEDIA_TYPE_415)
