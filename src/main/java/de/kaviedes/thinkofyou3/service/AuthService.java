package de.kaviedes.thinkofyou3.service;

import de.kaviedes.thinkofyou3.dto.AuthResponse;
import de.kaviedes.thinkofyou3.dto.LoginRequest;
import de.kaviedes.thinkofyou3.dto.RecoverPasswordRequest;
import de.kaviedes.thinkofyou3.dto.RecoveryCodeResponse;
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
    private final RecoveryEmailService recoveryEmailService;
    private final RecoveryRateLimitService recoveryRateLimitService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       RecoveryEmailService recoveryEmailService,
                       RecoveryRateLimitService recoveryRateLimitService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.recoveryEmailService = recoveryEmailService;
        this.recoveryRateLimitService = recoveryRateLimitService;
    }

    public RecoveryCodeResponse register(LoginRequest request) {
        validateCredentials(request.getUsername(), request.getPassword());
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        String recoveryCode = generateRecoveryCode();
        boolean sendEmail = recoveryEmailService.hasEmail(request.getRecoveryEmail());
        if (sendEmail) {
            recoveryRateLimitService.check("register-email:" + request.getUsername());
            recoveryEmailService.sendRecoveryCode(request.getRecoveryEmail(), request.getUsername(), recoveryCode);
        }

        User user = new User(request.getUsername(), passwordEncoder.encode(request.getPassword()));
        applyRecoveryCode(user, recoveryCode);
        userRepository.save(user);
        return new RecoveryCodeResponse(sendEmail ? null : recoveryCode, sendEmail);
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

    public RecoveryCodeResponse rotateRecoveryCode(String username, String currentPassword, String recoveryEmail) {
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new RuntimeException("Current password is required");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Current password is invalid");
        }

        String recoveryCode = generateRecoveryCode();
        boolean sendEmail = recoveryEmailService.hasEmail(recoveryEmail);
        if (sendEmail) {
            recoveryRateLimitService.check("profile-email:" + username);
            recoveryEmailService.sendRecoveryCode(recoveryEmail, username, recoveryCode);
        }

        applyRecoveryCode(user, recoveryCode);
        userRepository.save(user);
        return new RecoveryCodeResponse(sendEmail ? null : recoveryCode, sendEmail);
    }

    public RecoveryCodeResponse recoverPassword(RecoverPasswordRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new RuntimeException("Username is required");
        }
        if (request.getRecoveryCode() == null || request.getRecoveryCode().isBlank()) {
            throw new RuntimeException("Recovery code is required");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new RuntimeException("New password is required");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirmation do not match");
        }

        recoveryRateLimitService.check("recover:" + request.getUsername());
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid recovery code"));

        if (user.getRecoveryCodeHash() == null
                || !passwordEncoder.matches(request.getRecoveryCode(), user.getRecoveryCodeHash())) {
            throw new RuntimeException("Invalid recovery code");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new RuntimeException("New password must be different");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        String nextRecoveryCode = generateRecoveryCode();
        applyRecoveryCode(user, nextRecoveryCode);
        userRepository.save(user);
        revokeRefreshTokens(user.getId());
        return new RecoveryCodeResponse(nextRecoveryCode, false);
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtUtil.generateToken(user.getUsername());
        TokenPair refreshPair = createRefreshToken(user.getId());
        return new AuthResponse(accessToken, user.getUsername(), refreshPair.rawToken());
    }

    private void validateCredentials(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new RuntimeException("Username is required");
        }
        if (password == null || password.isBlank()) {
            throw new RuntimeException("Password is required");
        }
    }

    private String generateRecoveryCode() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder code = new StringBuilder("TOY");
        for (int group = 0; group < 4; group++) {
            code.append('-');
            for (int i = 0; i < 5; i++) {
                code.append(alphabet.charAt(secureRandom.nextInt(alphabet.length())));
            }
        }
        return code.toString();
    }

    private void applyRecoveryCode(User user, String recoveryCode) {
        Instant now = Instant.now();
        user.setRecoveryCodeHash(passwordEncoder.encode(recoveryCode));
        if (user.getRecoveryCodeCreatedAt() == null) {
            user.setRecoveryCodeCreatedAt(now);
        }
        user.setRecoveryCodeRotatedAt(now);
    }

    private void revokeRefreshTokens(String userId) {
        refreshTokenRepository.findByUserIdAndRevokedFalse(userId)
                .forEach(token -> {
                    token.setRevoked(true);
                    token.setLastUsedAt(Instant.now());
                    refreshTokenRepository.save(token);
                });
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
