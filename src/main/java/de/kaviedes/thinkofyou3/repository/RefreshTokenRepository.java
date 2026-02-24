package de.kaviedes.thinkofyou3.repository;

import de.kaviedes.thinkofyou3.model.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    Optional<RefreshToken> findByTokenHashAndRevokedFalseAndExpiresAtAfter(String tokenHash, Instant now);

    List<RefreshToken> findByUserIdAndRevokedFalse(String userId);
}
