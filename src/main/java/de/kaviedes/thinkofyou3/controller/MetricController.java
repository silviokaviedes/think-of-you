package de.kaviedes.thinkofyou3.controller;

import de.kaviedes.thinkofyou3.dto.MoodMetricsDTO;
import de.kaviedes.thinkofyou3.service.MetricService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
public class MetricController {
    private final MetricService metricService;

    public MetricController(MetricService metricService) {
        this.metricService = metricService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Long>> getMetrics(
            @RequestParam String connectionId,
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam int bucketMinutes,
            @RequestParam String direction,
            Authentication auth) {
        return ResponseEntity.ok(metricService.getMetrics(
                connectionId,
                auth.getName(),
                Instant.parse(from),
                Instant.parse(to),
                bucketMinutes,
                direction));
    }

    @GetMapping("/moods")
    public ResponseEntity<MoodMetricsDTO> getMoodMetrics(
            @RequestParam String connectionId,
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam int bucketMinutes,
            @RequestParam String direction,
            Authentication auth) {
        return ResponseEntity.ok(metricService.getMoodMetrics(
                connectionId,
                auth.getName(),
                Instant.parse(from),
                Instant.parse(to),
                bucketMinutes,
                direction));
    }
}
