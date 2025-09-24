package com.example.stopgap.exception

import com.example.stopgap.Endpoint
import com.example.stopgap.instanceregistry.InstanceRegistry
import io.helidon.http.HttpException
import io.helidon.http.Status
import io.helidon.webserver.http.HttpRules
import io.helidon.webserver.http.HttpService
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse

class ExceptionEndpoint : Endpoint {

    override fun routes(registry: InstanceRegistry): HttpService {
        return HttpService { rules: HttpRules ->
            rules
                .get("/bad-client-1", { req: ServerRequest, res: ServerResponse ->
                    throw BadClientException()
                })
                .get("/bad-client-2", { req: ServerRequest, res: ServerResponse ->
                    throw HttpException("Bad client request directly", Status.BAD_REQUEST_400)
                })
        }
    }

}
