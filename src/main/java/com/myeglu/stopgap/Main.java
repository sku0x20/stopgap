package com.myeglu.stopgap;

import io.helidon.config.Config;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.WebServerConfig;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        final var config = Config.create();

        final var server = WebServer.builder()
                .config(config.get("server"))
                .build();
        server.start();

        WebServerConfig serverConfig = server.prototype();
        System.out.println("Protocols: " + serverConfig.protocols());
    }

}
