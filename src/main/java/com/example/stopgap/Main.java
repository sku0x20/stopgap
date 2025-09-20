package com.example.stopgap;

import io.helidon.config.Config;
import io.helidon.webserver.WebServer;

public final class Main {

    private Main() {
    }

    static void main(final String[] args) {
        final var config = Config.create();
        final var mainEndpoint = new MainEndpoint();

        final var server = WebServer.builder()
            .config(config.get("server"))
            .protocolsDiscoverServices(false)
            .routing(mainEndpoint.routing())
            .build();
        server.start();
    }

}
