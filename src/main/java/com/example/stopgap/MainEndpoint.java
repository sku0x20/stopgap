package com.example.stopgap;

import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;

public final class MainEndpoint implements Endpoint {

    @Override
    public HttpService routes() {
        return (final HttpRules rules) -> rules
            .get("/i", (req, res) -> res.send("2nd"))
            .get("/2", (req, res) -> res.send("2nd"));
    }

}
