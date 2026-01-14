package de.kaviedes.thinkofyou3.service;

import de.kaviedes.thinkofyou3.model.Connection;
import de.kaviedes.thinkofyou3.model.ThoughtEvent;
import de.kaviedes.thinkofyou3.model.User;
import de.kaviedes.thinkofyou3.repository.ConnectionRepository;
import de.kaviedes.thinkofyou3.repository.ThoughtEventRepository;
import de.kaviedes.thinkofyou3.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MetricService {
    private final ThoughtEventRepository thoughtEventRepository;
    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;

    public MetricService(ThoughtEventRepository thoughtEventRepository, ConnectionRepository connectionRepository, UserRepository userRepository) {
        this.thoughtEventRepository = thoughtEventRepository;
        this.connectionRepository = connectionRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Long> getMetrics(String connectionId, String username, Instant from, Instant to, int bucketMinutes, String direction) {
        Connection c = connectionRepository.findById(connectionId).orElseThrow();
        User user = userRepository.findByUsername(username).orElseThrow();

        String senderId = null;
        String recipientId = null;

        if ("sent".equalsIgnoreCase(direction)) {
            senderId = user.getId();
        } else {
            recipientId = user.getId();
        }

        List<ThoughtEvent> events;
        if (senderId != null) {
            events = thoughtEventRepository.findByConnectionIdAndSenderIdAndOccurredAtBetween(connectionId, senderId, from, to);
        } else {
            events = thoughtEventRepository.findByConnectionIdAndRecipientIdAndOccurredAtBetween(connectionId, recipientId, from, to);
        }

        Map<String, Long> buckets = new LinkedHashMap<>();
        Instant current = from;
        while (current.isBefore(to)) {
            buckets.put(current.toString(), 0L);
            current = current.plus(bucketMinutes, ChronoUnit.MINUTES);
        }

        for (ThoughtEvent event : events) {
            Instant eventTime = event.getOccurredAt();
            // Find bucket
            long diff = ChronoUnit.MINUTES.between(from, eventTime);
            long bucketIndex = diff / bucketMinutes;
            Instant bucketStart = from.plus(bucketIndex * bucketMinutes, ChronoUnit.MINUTES);
            String key = bucketStart.toString();
            if (buckets.containsKey(key)) {
                buckets.put(key, buckets.get(key) + 1);
            }
        }

        return buckets;
    }
}
