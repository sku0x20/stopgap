package com.example.stopgap.generator.uuid;

import com.example.stopgap.generator.uuid.web.UuidEndpoint;
import com.example.stopgap.instanceregistry.Config;
import com.example.stopgap.instanceregistry.InstanceRegistry;
import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.testing.junit5.RoutingTest;
import io.helidon.webserver.testing.junit5.SetUpRoute;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

@RoutingTest
public final class UuidEndpointTest {

    private static final Config config = mock(Config.class);
    private static final InstanceRegistry registry = new InstanceRegistry(config);

    private static final UuidEndpoint endpoint = new UuidEndpoint(new UuidGen());

    private final Http1Client client;

    public UuidEndpointTest(Http1Client client) {
        this.client = client;
    }

    @SetUpRoute
    static void setUpRoute(final HttpRouting.Builder builder) {
        builder.register(endpoint.routes(registry));
    }

    @Test
    void returnsUuid() {
        try (final var response = client.get("/").request()) {
            assertThat(response.status()).isEqualTo(Status.OK_200);
        }
    }

}