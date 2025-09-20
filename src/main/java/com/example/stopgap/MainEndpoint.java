package com.example.stopgap;

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
        return (final HttpRules rules) -> rules
//            .register("/generate", )
            .get("/2", (req, res) -> res.send("2nd"));
    }

}
