package com.example.stopgap;

import io.helidon.config.Config;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;

public final class Main {

    private Main() {
    }

    public static void main(final String[] args) {
        final var config = Config.create();

        final HttpService r2 = (final HttpRules rules) -> rules
            .get("/i", (req, res) -> res.send("2nd"))
            .get("/2", (req, res) -> res.send("2nd"));

        final var routing = HttpRouting.builder()
            .get("/hello", (req, res) -> res.send("Hello World!"))
            .register("/inner", r2);

        final var server = WebServer.builder()
            .config(config.get("server"))
            .protocolsDiscoverServices(false)
            .routing(routing)
            .build();
        server.start();

        final WebServerConfig serverConfig = server.prototype();
        System.out.println("Protocols: " + serverConfig.protocols());
    }

}
