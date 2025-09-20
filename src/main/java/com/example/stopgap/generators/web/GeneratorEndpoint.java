package com.example.stopgap.generators.web;

import com.example.stopgap.Endpoint;
import com.example.stopgap.instanceregistry.InstanceRegistry;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import java.security.SecureRandom;

public final class GeneratorEndpoint implements Endpoint {

    private final SecureRandom random = new SecureRandom();

    @Override
    public HttpService routes(final InstanceRegistry registry) {
        return (final HttpRules rules) -> rules
            .get("/number", this::randomNumber);
    }

    private void randomNumber(
        final ServerRequest req,
        final ServerResponse res
    ) {
        res.send(random.nextLong());
    }

}
