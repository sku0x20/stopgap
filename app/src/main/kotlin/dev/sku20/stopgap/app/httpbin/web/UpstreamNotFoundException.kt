package dev.sku20.stopgap.app.httpbin.web

import io.helidon.http.HttpException
import io.helidon.http.Status

class UpstreamNotFoundException(path: String) :
    HttpException("Not found upstream: $path", Status.NOT_FOUND_404)
