package com.example.stopgap.exception;

import com.example.stopgap.Endpoint;
import com.example.stopgap.instanceregistry.InstanceRegistry;
import io.helidon.http.HttpException;
import io.helidon.http.Status;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;

public final class ExceptionEndpoint implements Endpoint {

    @Override
    public HttpService routes(final InstanceRegistry registry) {
        return (final HttpRules rules) -> {
            rules
                .get("/bad-client-1", (req, res) -> {
                    throw new BadClientException();
                })
                .get("/bad-client-2", (req, res) -> {
                    throw new HttpException("Bad client request directly", Status.BAD_REQUEST_400);
                });
        };
    }

}
