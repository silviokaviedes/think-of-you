package de.kaviedes.thinkofyou3.controller;

import de.kaviedes.thinkofyou3.dto.EventLogItemDTO;
import de.kaviedes.thinkofyou3.service.EventLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventLogControllerTest {

    @Mock
    private EventLogService eventLogService;

    @InjectMocks
    private EventLogController eventLogController;

    @Test
    void getEvents_returnsTimeline() {
        when(eventLogService.getEvents(eq("alice"), eq(10), eq("all"), eq(null)))
                .thenReturn(List.of(new EventLogItemDTO("c1", "bob", "sent", "happy", Instant.parse("2026-01-01T00:00:00Z"))));

        var response = eventLogController.getEvents(10, "all", null, auth());

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        List<EventLogItemDTO> body = (List<EventLogItemDTO>) response.getBody();
        assertThat(body).hasSize(1);
        assertThat(body.get(0).getPartnerUsername()).isEqualTo("bob");
    }

    @Test
    void getEvents_returnsBadRequestWhenServiceFails() {
        when(eventLogService.getEvents(eq("alice"), eq(10), eq("all"), eq(null)))
                .thenThrow(new RuntimeException("Unauthorized"));

        var response = eventLogController.getEvents(10, "all", null, auth());

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    private Authentication auth() {
        return new UsernamePasswordAuthenticationToken("alice", "n/a");
    }
}
