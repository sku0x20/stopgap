package com.example.stopgap;

import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;

final class MainEndpoint implements Endpoint {

    HttpRouting.Builder routing() {
        return HttpRouting.builder()
            .register("/", routes());
    }

    @Override
    public HttpService routes() {
        return (final HttpRules rules) -> rules
            .get("/i", (req, res) -> res.send("2nd"))
            .get("/2", (req, res) -> res.send("2nd"));
    }

}
