package com.example.stopgap.exception;

import com.example.stopgap.Endpoint;
import com.example.stopgap.instanceregistry.InstanceRegistry;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;

public final class ExceptionEndpoint implements Endpoint {

    @Override
    public HttpService routes(final InstanceRegistry registry) {
        return (final HttpRules rules) -> {
            rules.get("/bad-client", (req, res) -> {
                throw new BadClientException();
            });
        };
    }

}
