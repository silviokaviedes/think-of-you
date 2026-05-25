package de.kaviedes.thinkofyou3.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RecoveryEmailServiceTest {

    @Test
    void sendRecoveryCode_postsToResendApi() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RecoveryEmailService service = new RecoveryEmailService(
                builder,
                true,
                "Thinking of You <noreply@example.test>",
                "re_test_key",
                "https://api.resend.com/emails");

        server.expect(once(), requestTo("https://api.resend.com/emails"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer re_test_key"))
                .andExpect(jsonPath("$.from").value("Thinking of You <noreply@example.test>"))
                .andExpect(jsonPath("$.to[0]").value("alice@example.test"))
                .andExpect(jsonPath("$.subject").value("Your Thinking of You recovery code"))
                .andExpect(jsonPath("$.text").value(containsString("TOY-AAAAA-BBBBB-CCCCC-DDDDD")))
                .andRespond(withSuccess("{\"id\":\"email-id\"}", MediaType.APPLICATION_JSON));

        service.sendRecoveryCode(" alice@example.test ", "alice", "TOY-AAAAA-BBBBB-CCCCC-DDDDD");

        server.verify();
    }

    @Test
    void sendRecoveryCode_throwsWhenDisabled() {
        RecoveryEmailService service = new RecoveryEmailService(
                RestClient.builder(),
                false,
                "noreply@example.test",
                "re_test_key",
                "https://api.resend.com/emails");

        assertThatThrownBy(() -> service.sendRecoveryCode("alice@example.test", "alice", "code"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Recovery email delivery is not configured");
    }
}
