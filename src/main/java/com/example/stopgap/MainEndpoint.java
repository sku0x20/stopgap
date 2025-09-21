package com.example.stopgap;

import com.example.stopgap.exception.ExceptionEndpoint;
import com.example.stopgap.generator.web.GeneratorEndpoint;
import com.example.stopgap.instanceregistry.InstanceRegistry;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;

final class MainEndpoint implements Endpoint {

    HttpRouting.Builder routing(final InstanceRegistry registry) {
        return HttpRouting.builder()
            .register("/", routes(registry));
    }

    @Override
    public HttpService routes(final InstanceRegistry registry) {
        final var generator = registry.getInstanceForType(GeneratorEndpoint.class);
        final var exception = registry.getInstanceForType(ExceptionEndpoint.class);

        return (final HttpRules rules) -> rules
            .register("/generate", generator.routes(registry))
            .register("/exception", exception.routes(registry))
            .get("/ping", (req, res) -> res.send("pong"));
    }

}
