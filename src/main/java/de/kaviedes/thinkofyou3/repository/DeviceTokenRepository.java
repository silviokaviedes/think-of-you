package de.kaviedes.thinkofyou3.repository;

import de.kaviedes.thinkofyou3.model.DeviceToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends MongoRepository<DeviceToken, String> {
    Optional<DeviceToken> findByUserIdAndToken(String userId, String token);

    List<DeviceToken> findByUserId(String userId);

    void deleteByUserIdAndToken(String userId, String token);
}
