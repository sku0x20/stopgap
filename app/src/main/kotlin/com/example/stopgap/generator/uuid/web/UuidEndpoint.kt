package com.example.stopgap.generator.uuid.web

import com.example.stopgap.generator.uuid.UuidGen
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse

class UuidEndpoint(
    private val uuidGen: UuidGen
) {

    fun generateUuid(
        req: ServerRequest,
        res: ServerResponse
    ) {
        res.send(uuidGen.generate())
    }

}
