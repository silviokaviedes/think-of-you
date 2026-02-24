package de.kaviedes.thinkofyou3.service;

import de.kaviedes.thinkofyou3.dto.AuthResponse;
import de.kaviedes.thinkofyou3.dto.LoginRequest;
import de.kaviedes.thinkofyou3.model.RefreshToken;
import de.kaviedes.thinkofyou3.model.User;
import de.kaviedes.thinkofyou3.repository.RefreshTokenRepository;
import de.kaviedes.thinkofyou3.repository.UserRepository;
import de.kaviedes.thinkofyou3.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class AuthService {
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(60);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public void register(LoginRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        User user = new User(request.getUsername(), passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }
        return issueTokens(user);
    }

    public AuthResponse refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new RuntimeException("Refresh token is required");
        }

        String tokenHash = hashToken(rawRefreshToken);
        RefreshToken storedToken = refreshTokenRepository
                .findByTokenHashAndRevokedFalseAndExpiresAtAfter(tokenHash, Instant.now())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        TokenPair rotated = createRefreshToken(user.getId());
        storedToken.setRevoked(true);
        storedToken.setLastUsedAt(Instant.now());
        storedToken.setReplacedByTokenHash(rotated.tokenHash());
        refreshTokenRepository.save(storedToken);

        String accessToken = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(accessToken, user.getUsername(), rotated.rawToken());
    }

    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        String tokenHash = hashToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.setRevoked(true);
            token.setLastUsedAt(Instant.now());
            refreshTokenRepository.save(token);
        });
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public String getUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    public void changePassword(String username, String currentPassword, String newPassword) {
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new RuntimeException("Current password is required");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException("New password is required");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Current password is invalid");
        }
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new RuntimeException("New password must be different");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Force re-authentication on all devices after a password change.
        refreshTokenRepository.findByUserIdAndRevokedFalse(user.getId())
                .forEach(token -> {
                    token.setRevoked(true);
                    token.setLastUsedAt(Instant.now());
                    refreshTokenRepository.save(token);
                });
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtUtil.generateToken(user.getUsername());
        TokenPair refreshPair = createRefreshToken(user.getId());
        return new AuthResponse(accessToken, user.getUsername(), refreshPair.rawToken());
    }

    private TokenPair createRefreshToken(String userId) {
        String rawToken = generateSecureTokenValue();
        String tokenHash = hashToken(rawToken);
        RefreshToken refreshToken = new RefreshToken(
                userId,
                tokenHash,
                Instant.now().plus(REFRESH_TOKEN_TTL)
        );
        refreshTokenRepository.save(refreshToken);
        return new TokenPair(rawToken, tokenHash);
    }

    private String generateSecureTokenValue() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private record TokenPair(String rawToken, String tokenHash) {
    }
}
