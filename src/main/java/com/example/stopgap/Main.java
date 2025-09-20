package com.example.stopgap;

import io.helidon.config.Config;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.http.HttpRouting;

public final class Main {

    private Main() {
    }

    public static void main(final String[] args) {
        final var config = Config.create();

        final var mainEndpoint = new MainEndpoint();

        final var routing = HttpRouting.builder()
            .register("/", mainEndpoint.routes());
//            .get("/hello", (req, res) -> res.send("Hello World!"))

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
