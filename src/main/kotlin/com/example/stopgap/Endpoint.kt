package com.example.stopgap

import com.example.stopgap.instanceregistry.InstanceRegistry
import io.helidon.webserver.http.HttpService

interface Endpoint {
    fun routes(registry: InstanceRegistry): HttpService
}
