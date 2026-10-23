package dev.sku20.stopgap.app.httpbin.web

import io.helidon.http.HttpException
import io.helidon.http.Status

// 500, not the upstream's 4xx: a rejected request is our bug, not the caller's.
class UpstreamRejectedException(path: String, upstreamStatus: Status) :
    HttpException("httpbin rejected $path with $upstreamStatus", Status.INTERNAL_SERVER_ERROR_500)
