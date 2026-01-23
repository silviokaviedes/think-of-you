package de.kaviedes.thinkofyou3.service;

import de.kaviedes.thinkofyou3.dto.MoodMetricsDTO;
import de.kaviedes.thinkofyou3.model.Connection;
import de.kaviedes.thinkofyou3.model.Mood;
import de.kaviedes.thinkofyou3.model.ThoughtEvent;
import de.kaviedes.thinkofyou3.model.User;
import de.kaviedes.thinkofyou3.repository.ConnectionRepository;
import de.kaviedes.thinkofyou3.repository.ThoughtEventRepository;
import de.kaviedes.thinkofyou3.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class MetricServiceTest {

    @Mock
    private ThoughtEventRepository thoughtEventRepository;

    @Mock
    private ConnectionRepository connectionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MetricService metricService;

    @Test
    void getMetrics_bucketsEvents() {
        Connection connection = new Connection("u1", "u2");
        connection.setId("c1");
        User user = new User("alice", "hash");
        user.setId("u1");

        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to = Instant.parse("2024-01-01T02:00:00Z");

        ThoughtEvent event1 = new ThoughtEvent("c1", "u1", "u2", Mood.HAPPY);
        event1.setOccurredAt(Instant.parse("2024-01-01T00:10:00Z"));
        ThoughtEvent event2 = new ThoughtEvent("c1", "u1", "u2", Mood.SAD);
        event2.setOccurredAt(Instant.parse("2024-01-01T01:10:00Z"));

        when(connectionRepository.findById("c1")).thenReturn(Optional.of(connection));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(thoughtEventRepository.findByConnectionIdAndSenderIdAndOccurredAtBetween("c1", "u1", from, to))
                .thenReturn(List.of(event1, event2));

        Map<String, Long> buckets = metricService.getMetrics("c1", "alice", from, to, 60, "sent");

        assertThat(buckets.get("2024-01-01T00:00:00Z")).isEqualTo(1L);
        assertThat(buckets.get("2024-01-01T01:00:00Z")).isEqualTo(1L);
    }

    @Test
    void getMoodMetrics_includesDistribution() {
        Connection connection = new Connection("u1", "u2");
        connection.setId("c1");
        User user = new User("alice", "hash");
        user.setId("u2");

        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to = Instant.parse("2024-01-01T01:00:00Z");

        ThoughtEvent event = new ThoughtEvent("c1", "u1", "u2", Mood.GRATEFUL);
        event.setOccurredAt(Instant.parse("2024-01-01T00:30:00Z"));

        when(connectionRepository.findById("c1")).thenReturn(Optional.of(connection));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(thoughtEventRepository.findByConnectionIdAndRecipientIdAndOccurredAtBetween("c1", "u2", from, to))
                .thenReturn(List.of(event));

        MoodMetricsDTO result = metricService.getMoodMetrics("c1", "alice", from, to, 60, "received");

        assertThat(result.getTotalMoodDistribution().get(Mood.GRATEFUL)).isEqualTo(1L);
        assertThat(result.getTimeBuckets().get("2024-01-01T00:00:00Z").get(Mood.GRATEFUL)).isEqualTo(1L);
    }
}
