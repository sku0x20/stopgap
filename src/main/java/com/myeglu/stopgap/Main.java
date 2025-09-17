package com.myeglu.stopgap;

import io.helidon.config.Config;
import io.helidon.webserver.WebServer;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {

        final var config = Config.create();

        final var serverConfig = config.get("server");

        final var server = WebServer.builder()
                .config(serverConfig)
                .build();
        server.start();

        System.out.println("Server started at http://:" + server + server.port());
    }

}
