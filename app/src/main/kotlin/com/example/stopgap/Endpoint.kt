package com.example.stopgap

import dev.sku20.ir.InstanceRegistry
import io.helidon.webserver.http.HttpService

interface Endpoint {
    fun routes(registry: InstanceRegistry): HttpService {
        // todo: remove
        return HttpService {}
    }
}
