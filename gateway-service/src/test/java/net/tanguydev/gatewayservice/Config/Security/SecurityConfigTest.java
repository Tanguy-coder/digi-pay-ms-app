package net.tanguydev.gatewayservice.Config.Security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SecurityConfigTest {

    private WebTestClient webTestClient;

    @Autowired
    void setup(ApplicationContext context) {
        this.webTestClient = WebTestClient
                .bindToApplicationContext(context)
                .apply(springSecurity())
                .configureClient()
                .build();
    }

    @Test
    void actuator_isPublic() {
        webTestClient.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void protectedRoute_withoutToken_returns401() {
        webTestClient.get().uri("/api/v1/customers")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void protectedRoute_withValidJwt_isAllowed() {
        webTestClient
                .mutateWith(mockJwt().authorities(
                        new SimpleGrantedAuthority("ROLE_USER")
                ))
                .get().uri("/api/v1/customers")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void publicRoute_register_withoutToken_isAllowed() {
        webTestClient.post().uri("/api/v1/customers/register")
                .exchange()
                .expectStatus().isNotFound();
    }
}
