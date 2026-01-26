package de.kaviedes.thinkofyou3.service;

import de.kaviedes.thinkofyou3.dto.ConnectionDTO;
import de.kaviedes.thinkofyou3.model.Connection;
import de.kaviedes.thinkofyou3.model.Mood;
import de.kaviedes.thinkofyou3.model.ThoughtEvent;
import de.kaviedes.thinkofyou3.model.User;
import de.kaviedes.thinkofyou3.repository.ConnectionRepository;
import de.kaviedes.thinkofyou3.repository.ThoughtEventRepository;
import de.kaviedes.thinkofyou3.repository.UserRepository;
import de.kaviedes.thinkofyou3.service.PushNotificationService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConnectionService {
    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;
    private final ThoughtEventRepository thoughtEventRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final PushNotificationService pushNotificationService;

    public ConnectionService(ConnectionRepository connectionRepository, UserRepository userRepository,
                             ThoughtEventRepository thoughtEventRepository, SimpMessagingTemplate messagingTemplate,
                             PushNotificationService pushNotificationService) {
        this.connectionRepository = connectionRepository;
        this.userRepository = userRepository;
        this.thoughtEventRepository = thoughtEventRepository;
        this.messagingTemplate = messagingTemplate;
        this.pushNotificationService = pushNotificationService;
    }

    public void requestConnection(String requesterUsername, String recipientUsername) {
        User requester = userRepository.findByUsername(requesterUsername)
                .orElseThrow(() -> new RuntimeException("Requester not found"));
        User recipient = userRepository.findByUsername(recipientUsername)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        if (requester.getId().equals(recipient.getId())) {
            throw new RuntimeException("Cannot connect to yourself");
        }

        connectionRepository.findBetween(requester.getId(), recipient.getId()).ifPresent(c -> {
            if (c.getStatus() != Connection.Status.REJECTED && c.getStatus() != Connection.Status.DISCONNECTED) {
                throw new RuntimeException("Connection already exists or is pending");
            }
            connectionRepository.delete(c);
        });

        Connection connection = new Connection(requester.getId(), recipient.getId());
        connectionRepository.save(connection);
        notifyUpdate(recipient.getUsername());
    }

    public List<ConnectionDTO> getPendingRequests(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return connectionRepository.findByRecipientIdAndStatus(user.getId(), Connection.Status.PENDING)
                .stream()
                .map(c -> {
                    User requester = userRepository.findById(c.getRequesterId()).orElseThrow();
                    return new ConnectionDTO(c.getId(), requester.getUsername(), 0, 0, c.getStatus());
                })
                .collect(Collectors.toList());
    }

    public List<ConnectionDTO> getSentRequests(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return connectionRepository.findByRequesterIdAndStatus(user.getId(), Connection.Status.PENDING)
                .stream()
                .map(c -> {
                    User recipient = userRepository.findById(c.getRecipientId()).orElseThrow();
                    return new ConnectionDTO(c.getId(), recipient.getUsername(), 0, 0, c.getStatus());
                })
                .collect(Collectors.toList());
    }

    public List<ConnectionDTO> getAcceptedConnections(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return connectionRepository.findAllAccepted(user.getId())
                .stream()
                .map(c -> mapToDTO(c, user.getId()))
                .collect(Collectors.toList());
    }

    public void acceptConnection(String id, String username) {
        Connection c = connectionRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(username).orElseThrow();
        if (!c.getRecipientId().equals(user.getId())) throw new RuntimeException("Unauthorized");

        c.setStatus(Connection.Status.ACCEPTED);
        c.setUpdatedAt(Instant.now());
        connectionRepository.save(c);

        User requester = userRepository.findById(c.getRequesterId()).orElseThrow();
        notifyUpdate(requester.getUsername());
        notifyUpdate(username);
    }

    public void rejectConnection(String id, String username) {
        Connection c = connectionRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(username).orElseThrow();
        if (!c.getRecipientId().equals(user.getId())) throw new RuntimeException("Unauthorized");

        c.setStatus(Connection.Status.REJECTED);
        c.setUpdatedAt(Instant.now());
        connectionRepository.save(c);

        User requester = userRepository.findById(c.getRequesterId()).orElseThrow();
        notifyUpdate(requester.getUsername());
        notifyUpdate(username);
    }

    public void cancelConnection(String id, String username) {
        Connection c = connectionRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(username).orElseThrow();
        if (!c.getRequesterId().equals(user.getId())) throw new RuntimeException("Unauthorized");
        if (c.getStatus() != Connection.Status.PENDING) throw new RuntimeException("Cannot cancel non-pending request");

        connectionRepository.delete(c);

        User recipient = userRepository.findById(c.getRecipientId()).orElseThrow();
        notifyUpdate(recipient.getUsername());
        notifyUpdate(username);
    }

    public void deleteConnection(String id, String username) {
        Connection c = connectionRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(username).orElseThrow();
        if (!c.getRequesterId().equals(user.getId()) && !c.getRecipientId().equals(user.getId()))
            throw new RuntimeException("Unauthorized");

        connectionRepository.delete(c);

        String partnerId = c.getRequesterId().equals(user.getId()) ? c.getRecipientId() : c.getRequesterId();
        userRepository.findById(partnerId).ifPresent(p -> notifyUpdate(p.getUsername()));
        notifyUpdate(username);
    }

    public void thinkOfPartner(String id, String username) {
        thinkOfPartner(id, username, null);
    }

    public void thinkOfPartner(String id, String username, String moodValue) {
        Connection c = connectionRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(username).orElseThrow();
        if (c.getStatus() != Connection.Status.ACCEPTED) throw new RuntimeException("Connection not accepted");

        Mood mood = moodValue != null ? Mood.fromValue(moodValue) : Mood.NONE;

        String recipientId;
        if (c.getRequesterId().equals(user.getId())) {
            c.setRequesterToRecipientCount(c.getRequesterToRecipientCount() + 1);
            recipientId = c.getRecipientId();
        } else if (c.getRecipientId().equals(user.getId())) {
            c.setRecipientToRequesterCount(c.getRecipientToRequesterCount() + 1);
            recipientId = c.getRequesterId();
        } else {
            throw new RuntimeException("Unauthorized");
        }
        c.setUpdatedAt(Instant.now());
        connectionRepository.save(c);

        ThoughtEvent event = new ThoughtEvent(c.getId(), user.getId(), recipientId, mood);
        thoughtEventRepository.save(event);

        // Send specific notification to receiver
        userRepository.findById(recipientId).ifPresent(r -> {
            String moodEmoji = getMoodEmoji(mood);
            messagingTemplate.convertAndSend("/topic/updates/" + r.getUsername(), 
                String.format("{\"type\":\"thought\",\"sender\":\"%s\",\"mood\":\"%s\",\"emoji\":\"%s\"}", 
                    username, mood.getValue(), moodEmoji));
        });

        // Fire push notification to recipient devices (no-op if FCM isn't configured).
        pushNotificationService.sendThoughtNotification(recipientId, username, mood);
        
        // Send refresh to sender
        notifyUpdate(username);
    }

    private ConnectionDTO mapToDTO(Connection c, String userId) {
        String partnerId = c.getRequesterId().equals(userId) ? c.getRecipientId() : c.getRequesterId();
        User partner = userRepository.findById(partnerId).orElseThrow();
        int sent = c.getRequesterId().equals(userId) ? c.getRequesterToRecipientCount() : c.getRecipientToRequesterCount();
        int received = c.getRequesterId().equals(userId) ? c.getRecipientToRequesterCount() : c.getRequesterToRecipientCount();

        // Find the last received mood
        Mood lastReceivedMood = thoughtEventRepository
                .findFirstByConnectionIdAndRecipientIdOrderByOccurredAtDesc(c.getId(), userId)
                .map(event -> event.getMood() != null ? event.getMood() : Mood.NONE)
                .orElse(null);

        // Find the last sent mood
        Mood lastSentMood = thoughtEventRepository
                .findFirstByConnectionIdAndSenderIdOrderByOccurredAtDesc(c.getId(), userId)
                .map(event -> event.getMood() != null ? event.getMood() : Mood.NONE)
                .orElse(null);

        return new ConnectionDTO(c.getId(), partner.getUsername(), received, sent, c.getStatus(), lastReceivedMood, lastSentMood);
    }

    private void notifyUpdate(String username) {
        messagingTemplate.convertAndSend("/topic/updates/" + username, "refresh");
    }
    
    private String getMoodEmoji(Mood mood) {
        switch (mood) {
            case HAPPY: return "😊";
            case SAD: return "😢";
            case ANGRY: return "😠";
            case LOVE: return "❤️";
            case EXCITED: return "🤗";
            case WORRIED: return "😟";
            case GRATEFUL: return "🙏";
            case NONE: return "💭";
            default: return "💭";
        }
    }
}
