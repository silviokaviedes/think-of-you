package de.kaviedes.thinkofyou3.repository;

import de.kaviedes.thinkofyou3.model.DeviceToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends MongoRepository<DeviceToken, String> {
    /**
     * Finds a token record for a specific user and token value.
     *
     * @param userId user id owning the token
     * @param token  raw FCM device token
     * @return device token record if present
     */
    Optional<DeviceToken> findByUserIdAndToken(String userId, String token);

    /**
     * Returns all device tokens registered for a user.
     *
     * @param userId user id owning the tokens
     * @return list of device tokens
     */
    List<DeviceToken> findByUserId(String userId);

    /**
     * Deletes a specific token for a user, typically after a failed FCM send.
     *
     * @param userId user id owning the token
     * @param token  raw FCM device token
     */
    void deleteByUserIdAndToken(String userId, String token);
}
