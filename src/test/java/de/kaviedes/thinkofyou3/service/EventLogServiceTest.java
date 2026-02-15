package de.kaviedes.thinkofyou3.service;

import de.kaviedes.thinkofyou3.dto.EventLogItemDTO;
import de.kaviedes.thinkofyou3.model.Connection;
import de.kaviedes.thinkofyou3.model.Mood;
import de.kaviedes.thinkofyou3.model.ThoughtEvent;
import de.kaviedes.thinkofyou3.model.User;
import de.kaviedes.thinkofyou3.repository.ConnectionRepository;
import de.kaviedes.thinkofyou3.repository.ThoughtEventRepository;
import de.kaviedes.thinkofyou3.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventLogServiceTest {

    @Mock
    private ThoughtEventRepository thoughtEventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ConnectionRepository connectionRepository;

    @InjectMocks
    private EventLogService eventLogService;

    @Test
    void getEvents_returnsMergedTimelineWithLimit() {
        User alice = new User("alice", "hash");
        alice.setId("u1");
        User bob = new User("bob", "hash");
        bob.setId("u2");

        ThoughtEvent sent = new ThoughtEvent("c1", "u1", "u2", Mood.HAPPY);
        sent.setOccurredAt(Instant.parse("2026-01-01T10:00:00Z"));
        ThoughtEvent received = new ThoughtEvent("c1", "u2", "u1", Mood.LOVE);
        received.setOccurredAt(Instant.parse("2026-01-01T11:00:00Z"));

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(thoughtEventRepository.findTop200BySenderIdOrderByOccurredAtDesc("u1")).thenReturn(List.of(sent));
        when(thoughtEventRepository.findTop200ByRecipientIdOrderByOccurredAtDesc("u1")).thenReturn(List.of(received));
        when(userRepository.findAllById(org.mockito.ArgumentMatchers.anyIterable())).thenReturn(List.of(alice, bob));

        List<EventLogItemDTO> events = eventLogService.getEvents("alice", 1, "all", null);

        assertThat(events).hasSize(1);
        assertThat(events.get(0).getDirection()).isEqualTo("received");
        assertThat(events.get(0).getPartnerUsername()).isEqualTo("bob");
    }

    @Test
    void getEvents_throwsWhenConnectionUnauthorized() {
        User alice = new User("alice", "hash");
        alice.setId("u1");
        Connection connection = new Connection("u2", "u3");
        connection.setId("c1");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(connectionRepository.findById("c1")).thenReturn(Optional.of(connection));

        assertThatThrownBy(() -> eventLogService.getEvents("alice", 25, "all", "c1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unauthorized");
    }
}
