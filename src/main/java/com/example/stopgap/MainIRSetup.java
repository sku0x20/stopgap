package com.example.stopgap;

import com.example.stopgap.instanceregistry.InstanceRegistry;

final class MainIRSetup {
    private MainIRSetup() {
    }

    static void setup(final InstanceRegistry registry) {
        registry.registerForType(MainEndpoint.class, MainIRSetup::mainEndpoint);
    }

    private static MainEndpoint mainEndpoint(final InstanceRegistry registry) {
        return new MainEndpoint();
    }
}
