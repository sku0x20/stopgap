package dev.sku20.stopgap.helidon.serde

import io.helidon.http.HttpException
import io.helidon.http.Status

// Generic message: the parser's own message leaks library internals to the client. It stays on the cause for logs.
class MalformedBodyException(cause: Throwable?) :
    HttpException("Malformed request body", Status.BAD_REQUEST_400, cause)
