package com.example.stopgap;

import io.helidon.webserver.http.HttpService;

public interface Endpoint {
    HttpService routes();
}
