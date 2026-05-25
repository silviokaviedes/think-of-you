package de.kaviedes.thinkofyou3.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Service
public class RecoveryEmailService {
    private final boolean enabled;
    private final String fromAddress;
    private final String resendApiKey;
    private final RestClient restClient;

    public RecoveryEmailService(RestClient.Builder restClientBuilder,
                                @Value("${app.recovery.mail.enabled:false}") boolean enabled,
                                @Value("${app.recovery.mail.from:noreply@think-of-you.local}") String fromAddress,
                                @Value("${app.recovery.mail.resend-api-key:}") String resendApiKey,
                                @Value("${app.recovery.mail.resend-api-url:https://api.resend.com/emails}") String resendApiUrl) {
        this.enabled = enabled;
        this.fromAddress = fromAddress;
        this.resendApiKey = resendApiKey;
        this.restClient = restClientBuilder.baseUrl(resendApiUrl).build();
    }

    public void sendRecoveryCode(String toAddress, String username, String recoveryCode) {
        String normalizedAddress = normalizeEmail(toAddress);
        if (!enabled || resendApiKey == null || resendApiKey.isBlank()) {
            throw new RuntimeException("Recovery email delivery is not configured");
        }

        Map<String, Object> request = Map.of(
                "from", fromAddress,
                "to", List.of(normalizedAddress),
                "subject", "Your Thinking of You recovery code",
                "text", """
                        A recovery code was requested for your Thinking of You account.

                        Username: %s
                        Recovery code: %s

                        Keep this code private. The app does not store this email address.
                        """.formatted(username, recoveryCode)
        );

        try {
            restClient.post()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + resendApiKey)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            throw new RuntimeException("Failed to send recovery email");
        }
    }

    public boolean hasEmail(String email) {
        return email != null && !email.isBlank();
    }

    public String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Recovery email is required");
        }
        String normalized = email.trim();
        if (!normalized.contains("@") || normalized.startsWith("@") || normalized.endsWith("@")) {
            throw new RuntimeException("Recovery email is invalid");
        }
        return normalized;
    }
}
