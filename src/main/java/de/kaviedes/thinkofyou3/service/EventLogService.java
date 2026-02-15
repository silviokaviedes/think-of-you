package de.kaviedes.thinkofyou3.service;

import de.kaviedes.thinkofyou3.dto.EventLogItemDTO;
import de.kaviedes.thinkofyou3.model.Connection;
import de.kaviedes.thinkofyou3.model.ThoughtEvent;
import de.kaviedes.thinkofyou3.model.User;
import de.kaviedes.thinkofyou3.repository.ConnectionRepository;
import de.kaviedes.thinkofyou3.repository.ThoughtEventRepository;
import de.kaviedes.thinkofyou3.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EventLogService {
    private static final int DEFAULT_LIMIT = 25;
    private static final int MAX_LIMIT = 200;

    private final ThoughtEventRepository thoughtEventRepository;
    private final UserRepository userRepository;
    private final ConnectionRepository connectionRepository;

    public EventLogService(
            ThoughtEventRepository thoughtEventRepository,
            UserRepository userRepository,
            ConnectionRepository connectionRepository) {
        this.thoughtEventRepository = thoughtEventRepository;
        this.userRepository = userRepository;
        this.connectionRepository = connectionRepository;
    }

    public List<EventLogItemDTO> getEvents(String username, Integer limit, String direction, String connectionId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String userId = user.getId();

        if (connectionId != null && !connectionId.isBlank()) {
            ensureConnectionMembership(connectionId, userId);
        }

        int safeLimit = normalizeLimit(limit);
        String normalizedDirection = normalizeDirection(direction);

        List<ThoughtEvent> events = new ArrayList<>();
        if (!"received".equals(normalizedDirection)) {
            events.addAll(fetchSentEvents(userId, connectionId));
        }
        if (!"sent".equals(normalizedDirection)) {
            events.addAll(fetchReceivedEvents(userId, connectionId));
        }

        Map<String, String> usernameById = userRepository.findAllById(events.stream()
                        .flatMap(event -> java.util.stream.Stream.of(event.getSenderId(), event.getRecipientId()))
                        .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(User::getId, User::getUsername, (left, right) -> left));

        return events.stream()
                .sorted(Comparator.comparing(ThoughtEvent::getOccurredAt).reversed())
                .limit(safeLimit)
                .map(event -> toDto(event, userId, usernameById))
                .collect(Collectors.toList());
    }

    private List<ThoughtEvent> fetchSentEvents(String userId, String connectionId) {
        if (connectionId != null && !connectionId.isBlank()) {
            return thoughtEventRepository.findTop200ByConnectionIdAndSenderIdOrderByOccurredAtDesc(connectionId, userId);
        }
        return thoughtEventRepository.findTop200BySenderIdOrderByOccurredAtDesc(userId);
    }

    private List<ThoughtEvent> fetchReceivedEvents(String userId, String connectionId) {
        if (connectionId != null && !connectionId.isBlank()) {
            return thoughtEventRepository.findTop200ByConnectionIdAndRecipientIdOrderByOccurredAtDesc(connectionId, userId);
        }
        return thoughtEventRepository.findTop200ByRecipientIdOrderByOccurredAtDesc(userId);
    }

    private EventLogItemDTO toDto(ThoughtEvent event, String userId, Map<String, String> usernameById) {
        boolean sent = userId.equals(event.getSenderId());
        String partnerId = sent ? event.getRecipientId() : event.getSenderId();
        String partnerUsername = usernameById.getOrDefault(partnerId, "unknown");
        String mood = event.getMood() != null ? event.getMood().getValue() : "none";
        return new EventLogItemDTO(
                event.getConnectionId(),
                partnerUsername,
                sent ? "sent" : "received",
                mood,
                event.getOccurredAt());
    }

    private int normalizeLimit(Integer limit) {
        int value = limit == null ? DEFAULT_LIMIT : limit;
        if (value < 1) return DEFAULT_LIMIT;
        return Math.min(value, MAX_LIMIT);
    }

    private String normalizeDirection(String direction) {
        if ("sent".equalsIgnoreCase(direction)) return "sent";
        if ("received".equalsIgnoreCase(direction)) return "received";
        return "all";
    }

    private void ensureConnectionMembership(String connectionId, String userId) {
        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new RuntimeException("Connection not found"));
        boolean isRequester = userId.equals(connection.getRequesterId());
        boolean isRecipient = userId.equals(connection.getRecipientId());
        if (!isRequester && !isRecipient) {
            throw new RuntimeException("Unauthorized");
        }
    }
}
