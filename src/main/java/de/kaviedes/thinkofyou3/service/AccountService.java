package de.kaviedes.thinkofyou3.service;

import de.kaviedes.thinkofyou3.model.Connection;
import de.kaviedes.thinkofyou3.model.User;
import de.kaviedes.thinkofyou3.repository.ConnectionRepository;
import de.kaviedes.thinkofyou3.repository.DeviceTokenRepository;
import de.kaviedes.thinkofyou3.repository.RefreshTokenRepository;
import de.kaviedes.thinkofyou3.repository.ThoughtEventRepository;
import de.kaviedes.thinkofyou3.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class AccountService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final ConnectionRepository connectionRepository;
    private final ThoughtEventRepository thoughtEventRepository;
    private final PasswordEncoder passwordEncoder;
    private final SimpMessagingTemplate messagingTemplate;

    public AccountService(UserRepository userRepository,
                          RefreshTokenRepository refreshTokenRepository,
                          DeviceTokenRepository deviceTokenRepository,
                          ConnectionRepository connectionRepository,
                          ThoughtEventRepository thoughtEventRepository,
                          PasswordEncoder passwordEncoder,
                          SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.connectionRepository = connectionRepository;
        this.thoughtEventRepository = thoughtEventRepository;
        this.passwordEncoder = passwordEncoder;
        this.messagingTemplate = messagingTemplate;
    }

    public void deleteAccount(String username, String currentPassword) {
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new RuntimeException("Current password is required");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Current password is invalid");
        }

        List<Connection> connections = connectionRepository.findByRequesterIdOrRecipientId(user.getId(), user.getId());
        List<String> connectionIds = connections.stream()
                .map(Connection::getId)
                .filter(Objects::nonNull)
                .toList();

        List<String> affectedPartnerUsernames = connections.stream()
                .map(connection -> connection.getRequesterId().equals(user.getId())
                        ? connection.getRecipientId()
                        : connection.getRequesterId())
                .distinct()
                .map(partnerId -> userRepository.findById(partnerId).map(User::getUsername).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        if (!connectionIds.isEmpty()) {
            thoughtEventRepository.deleteByConnectionIdIn(connectionIds);
        }
        thoughtEventRepository.deleteBySenderIdOrRecipientId(user.getId(), user.getId());

        if (!connections.isEmpty()) {
            connectionRepository.deleteAll(connections);
        }

        refreshTokenRepository.deleteByUserId(user.getId());
        deviceTokenRepository.deleteByUserId(user.getId());
        userRepository.delete(user);

        affectedPartnerUsernames.forEach(this::notifyUpdate);
    }

    private void notifyUpdate(String username) {
        messagingTemplate.convertAndSend("/topic/updates/" + username, "refresh");
    }
}
