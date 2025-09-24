package com.example.stopgap.generator.uuid.web

import com.example.stopgap.Endpoint
import com.example.stopgap.generator.uuid.UuidGen
import com.example.stopgap.instanceregistry.InstanceRegistry
import io.helidon.webserver.http.HttpRules
import io.helidon.webserver.http.HttpService
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse

class UuidEndpoint(
    private val uuidGen: UuidGen
) : Endpoint {

    override fun routes(registry: InstanceRegistry): HttpService {
        return HttpService { rules: HttpRules ->
            rules
                .get("/", ::generateUuid)
        }
    }

    private fun generateUuid(
        req: ServerRequest,
        res: ServerResponse
    ) {
        res.send(uuidGen.generate())
    }

}
