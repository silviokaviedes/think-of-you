package de.kaviedes.thinkofyou3.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import de.kaviedes.thinkofyou3.model.DeviceToken;
import de.kaviedes.thinkofyou3.model.Mood;
import de.kaviedes.thinkofyou3.repository.DeviceTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class PushNotificationService {
    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);
    private final DeviceTokenRepository deviceTokenRepository;
    private final boolean enabled;

    /**
     * Initializes the push notification service and attempts to configure Firebase.
     * If credentials are missing or invalid, push notifications are disabled.
     *
     * @param deviceTokenRepository repository used to store and clean up device tokens
     */
    public PushNotificationService(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
        this.enabled = initializeFirebase();
    }

    /**
     * Registers or updates a device token for a given user.
     * If the token already exists, its platform and timestamps are refreshed.
     *
     * @param userId   user id owning the token
     * @param token    raw FCM device token
     * @param platform platform identifier (e.g. android, ios)
     */
    public void registerToken(String userId, String token, String platform) {
        if (token == null || token.isBlank()) {
            throw new RuntimeException("Token is required");
        }
        // Normalize platform for consistent storage.
        String normalizedPlatform = platform == null ? "android" : platform.toLowerCase(Locale.ROOT);

        Optional<DeviceToken> existing = deviceTokenRepository.findByUserIdAndToken(userId, token);
        if (existing.isPresent()) {
            DeviceToken deviceToken = existing.get();
            deviceToken.setPlatform(normalizedPlatform);
            deviceToken.touch();
            deviceTokenRepository.save(deviceToken);
            return;
        }

        DeviceToken deviceToken = new DeviceToken(userId, token, normalizedPlatform);
        deviceTokenRepository.save(deviceToken);
    }

    /**
     * Sends a push notification about a new thought to all devices registered for the recipient.
     * If Firebase isn't configured, the call is a no-op.
     *
     * @param recipientId    user id receiving the thought
     * @param senderUsername username of the sender
     * @param mood           mood value attached to the thought
     */
    public void sendThoughtNotification(String recipientId, String senderUsername, Mood mood) {
        if (!enabled) {
            return;
        }

        List<DeviceToken> tokens = deviceTokenRepository.findByUserId(recipientId);
        if (tokens.isEmpty()) {
            return;
        }

        // Multicast to all active device tokens for the recipient.
        List<String> rawTokens = tokens.stream().map(DeviceToken::getToken).toList();

        String emoji = getMoodEmoji(mood);
        Notification notification = Notification.builder()
                .setTitle("Someone is thinking of you")
                .setBody(senderUsername + " is thinking of you " + emoji)
                .build();

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(rawTokens)
                .setNotification(notification)
                .putData("type", "thought")
                .putData("sender", senderUsername)
                .putData("mood", mood.getValue())
                .putData("emoji", emoji)
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
            // Remove invalid tokens reported by FCM to keep the list clean.
            cleanupInvalidTokens(tokens, response);
        } catch (Exception ex) {
            log.error("Failed to send FCM notification", ex);
        }
    }

    /**
     * Removes tokens that FCM reports as invalid.
     *
     * @param tokens    list of tokens used in the multicast send
     * @param response  FCM batch response, aligned with token order
     */
    private void cleanupInvalidTokens(List<DeviceToken> tokens, BatchResponse response) {
        if (response.getFailureCount() == 0) {
            return;
        }

        List<DeviceToken> invalidTokens = IntStream
                .range(0, tokens.size())
                .filter(i -> !response.getResponses().get(i).isSuccessful())
                .mapToObj(tokens::get)
                .collect(Collectors.toList());

        for (DeviceToken invalid : invalidTokens) {
            deviceTokenRepository.deleteByUserIdAndToken(invalid.getUserId(), invalid.getToken());
        }
    }

    /**
     * Initializes Firebase Admin SDK using environment-provided credentials.
     * The following sources are checked in order:
     * - FIREBASE_SERVICE_ACCOUNT_BASE64 (base64 JSON)
     * - FIREBASE_SERVICE_ACCOUNT_PATH (filesystem path)
     * - GOOGLE_APPLICATION_CREDENTIALS (filesystem path)
     *
     * @return true if Firebase initialized successfully
     */
    private boolean initializeFirebase() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return true;
        }

        try {
            byte[] credentialsBytes = resolveCredentials();
            if (credentialsBytes == null) {
                log.warn("Firebase credentials not configured; push notifications are disabled.");
                return false;
            }
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(credentialsBytes)))
                    .build();
            FirebaseApp.initializeApp(options);
            return true;
        } catch (Exception ex) {
            log.error("Failed to initialize Firebase; push notifications disabled.", ex);
            return false;
        }
    }

    /**
     * Resolves Firebase service account credentials from environment configuration.
     *
     * @return credentials as bytes, or null if no credentials are configured
     */
    private byte[] resolveCredentials() throws IOException {
        String base64 = System.getenv("FIREBASE_SERVICE_ACCOUNT_BASE64");
        if (base64 != null && !base64.isBlank()) {
            // Prefer env var base64 for container-friendly deployments.
            return Base64.getDecoder().decode(base64);
        }

        String path = System.getenv("FIREBASE_SERVICE_ACCOUNT_PATH");
        if (path == null || path.isBlank()) {
            path = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        }
        if (path == null || path.isBlank()) {
            return null;
        }

        // Fall back to filesystem path if provided.
        return Files.readAllBytes(Path.of(path));
    }

    /**
     * Maps a mood enum to a readable emoji for notifications.
     *
     * @param mood mood value
     * @return emoji string
     */
    private String getMoodEmoji(Mood mood) {
        return switch (mood) {
            case HAPPY -> "ðŸ˜Š";
            case SAD -> "ðŸ˜¢";
            case ANGRY -> "ðŸ˜ ";
            case LOVE -> "â¤ï¸";
            case EXCITED -> "ðŸ¤—";
            case WORRIED -> "ðŸ˜Ÿ";
            case GRATEFUL -> "ðŸ™";
            case NONE -> "ðŸ’­";
        };
    }

    
}
