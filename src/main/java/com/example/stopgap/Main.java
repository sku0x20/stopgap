package com.example.stopgap;

import com.example.stopgap.instanceregistry.InstanceRegistry;
import io.helidon.config.Config;
import io.helidon.webserver.WebServer;

public final class Main {

    private Main() {
    }

    static void main(final String[] args) {
        final var config = Config.create();

        final com.example.stopgap.instanceregistry.Config config1 = (key) -> "test";
        final var instanceRegistry = new InstanceRegistry(config1);
        MainConfig.setup(instanceRegistry);

        final var mainEndpoint = instanceRegistry.getInstanceForType(MainEndpoint.class);

        final var server = WebServer.builder()
            .config(config.get("server"))
            .protocolsDiscoverServices(false)
            .routing(mainEndpoint.routing(instanceRegistry))
            .build();
        server.start();
    }

}
