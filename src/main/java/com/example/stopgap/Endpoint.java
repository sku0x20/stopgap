package com.example.stopgap;

import com.example.stopgap.instanceregistry.InstanceRegistry;
import io.helidon.webserver.http.HttpService;

public interface Endpoint {
    HttpService routes(final InstanceRegistry registry);
}
