package net.tanguydev.gatewayservice.Config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class RateLimiterConfigTest {

    @Container
    @ServiceConnection
    @SuppressWarnings("resource")
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @Autowired
    private KeyResolver userKeyResolver;

    @Test
    void keyResolver_beanIsPresent() {
        assertThat(userKeyResolver).isNotNull();
    }

    @Test
    void keyResolver_anonymousRequest_returnsIpAddress() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/payments")
                .remoteAddress(new java.net.InetSocketAddress("192.168.1.1", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(userKeyResolver.resolve(exchange))
                .assertNext(key -> assertThat(key).isEqualTo("192.168.1.1"))
                .verifyComplete();
    }
}
