package dev.sku20.stopgap.helidon.serde

import io.helidon.http.HttpException
import io.helidon.http.HttpMediaType
import io.helidon.http.Status

class NotAcceptableException(mediaTypes: List<HttpMediaType>) :
    HttpException("No serde available for $mediaTypes", Status.NOT_ACCEPTABLE_406)
