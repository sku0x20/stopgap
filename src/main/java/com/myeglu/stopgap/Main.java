package com.myeglu.stopgap;

import io.helidon.config.Config;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.http2.Http2Config;

import java.util.List;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        final var config = Config.create();

        final var http2Config = Http2Config.builder().build();

        final var server = WebServer.builder()
                .config(config.get("server"))
                .protocolsDiscoverServices(false)
                .protocols(List.of(http2Config))
                .build();
        server.start();

        WebServerConfig serverConfig = server.prototype();
        System.out.println("Protocols: " + serverConfig.protocols());
    }

}
