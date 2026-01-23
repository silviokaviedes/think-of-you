package de.kaviedes.thinkofyou3.controller;

import de.kaviedes.thinkofyou3.dto.MoodMetricsDTO;
import de.kaviedes.thinkofyou3.model.Mood;
import de.kaviedes.thinkofyou3.service.MetricService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricControllerTest {

    @Mock
    private MetricService metricService;

    @InjectMocks
    private MetricController metricController;

    @Test
    void getMetrics_returnsBuckets() {
        Map<String, Long> buckets = new LinkedHashMap<>();
        buckets.put(Instant.parse("2024-01-01T00:00:00Z").toString(), 2L);

        when(metricService.getMetrics(any(), any(), any(), any(), any(Integer.class), any()))
                .thenReturn(buckets);

        var response = metricController.getMetrics(
                "c1",
                "2024-01-01T00:00:00Z",
                "2024-01-02T00:00:00Z",
                60,
                "received",
                auth());

        assertThat(response.getBody()).containsEntry("2024-01-01T00:00:00Z", 2L);
    }

    @Test
    void getMoodMetrics_returnsDistribution() {
        Map<String, Map<Mood, Long>> timeBuckets = new LinkedHashMap<>();
        Map<Mood, Long> moodCounts = new LinkedHashMap<>();
        moodCounts.put(Mood.HAPPY, 1L);
        timeBuckets.put("2024-01-01T00:00:00Z", moodCounts);

        Map<Mood, Long> totals = new LinkedHashMap<>();
        totals.put(Mood.HAPPY, 1L);

        when(metricService.getMoodMetrics(any(), any(), any(), any(), any(Integer.class), any()))
                .thenReturn(new MoodMetricsDTO(timeBuckets, totals));

        var response = metricController.getMoodMetrics(
                "c1",
                "2024-01-01T00:00:00Z",
                "2024-01-02T00:00:00Z",
                60,
                "sent",
                auth());

        assertThat(response.getBody().getTotalMoodDistribution().get(Mood.HAPPY)).isEqualTo(1L);
        assertThat(response.getBody().getTimeBuckets().get("2024-01-01T00:00:00Z").get(Mood.HAPPY)).isEqualTo(1L);
    }

    private Authentication auth() {
        return new UsernamePasswordAuthenticationToken("alice", "n/a");
    }
}
