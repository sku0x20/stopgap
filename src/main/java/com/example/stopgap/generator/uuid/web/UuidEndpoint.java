package com.example.stopgap.generator.uuid.web;

import com.example.stopgap.Endpoint;
import com.example.stopgap.generator.uuid.UuidGen;
import com.example.stopgap.instanceregistry.InstanceRegistry;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

public final class UuidEndpoint implements Endpoint {

    private final UuidGen uudiGen;

    public UuidEndpoint(
        final UuidGen uuidGen
    ) {
        this.uudiGen = uuidGen;
    }

    @Override
    public HttpService routes(final InstanceRegistry registry) {
        return (final HttpRules rules) -> rules
            .get("/", this::generateUuid);
    }

    private void generateUuid(
        final ServerRequest req,
        final ServerResponse res
    ) {
        res.send(uudiGen.generate());
    }

}
