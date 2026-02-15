package de.kaviedes.thinkofyou3.service;

import de.kaviedes.thinkofyou3.dto.MoodMetricsDTO;
import de.kaviedes.thinkofyou3.model.Connection;
import de.kaviedes.thinkofyou3.model.Mood;
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
import java.util.stream.Collectors;

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
        ensureUserIsConnectionParticipant(c, user);

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

    public MoodMetricsDTO getMoodMetrics(String connectionId, String username, Instant from, Instant to, int bucketMinutes, String direction) {
        Connection c = connectionRepository.findById(connectionId).orElseThrow();
        User user = userRepository.findByUsername(username).orElseThrow();
        ensureUserIsConnectionParticipant(c, user);

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

        // Initialize time buckets with mood maps
        Map<String, Map<Mood, Long>> moodBuckets = new LinkedHashMap<>();
        Instant current = from;
        while (current.isBefore(to)) {
            Map<Mood, Long> moodMap = new LinkedHashMap<>();
            for (Mood mood : Mood.values()) {
                moodMap.put(mood, 0L);
            }
            moodBuckets.put(current.toString(), moodMap);
            current = current.plus(bucketMinutes, ChronoUnit.MINUTES);
        }

        // Aggregate mood data by time bucket
        for (ThoughtEvent event : events) {
            Instant eventTime = event.getOccurredAt();
            long diff = ChronoUnit.MINUTES.between(from, eventTime);
            long bucketIndex = diff / bucketMinutes;
            Instant bucketStart = from.plus(bucketIndex * bucketMinutes, ChronoUnit.MINUTES);
            String key = bucketStart.toString();
            
            if (moodBuckets.containsKey(key)) {
                Map<Mood, Long> moodMap = moodBuckets.get(key);
                Mood mood = event.getMood() != null ? event.getMood() : Mood.NONE;
                moodMap.put(mood, moodMap.get(mood) + 1);
            }
        }

        // Calculate total mood distribution
        Map<Mood, Long> totalMoodDistribution = events.stream()
            .collect(Collectors.groupingBy(
                event -> event.getMood() != null ? event.getMood() : Mood.NONE,
                Collectors.counting()
            ));

        return new MoodMetricsDTO(moodBuckets, totalMoodDistribution);
    }

    private void ensureUserIsConnectionParticipant(Connection connection, User user) {
        boolean isRequester = user.getId().equals(connection.getRequesterId());
        boolean isRecipient = user.getId().equals(connection.getRecipientId());
        if (!isRequester && !isRecipient) {
            throw new RuntimeException("Unauthorized");
        }
    }
}
