package de.kaviedes.thinkofyou3.repository;

import de.kaviedes.thinkofyou3.model.Connection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ConnectionRepository extends MongoRepository<Connection, String> {
    @Query("{ '$or': [ { 'requesterId': ?0, 'recipientId': ?1 }, { 'requesterId': ?1, 'recipientId': ?0 } ] }")
    Optional<Connection> findBetween(String userId1, String userId2);

    List<Connection> findByRequesterIdAndStatus(String requesterId, Connection.Status status);
    List<Connection> findByRecipientIdAndStatus(String recipientId, Connection.Status status);

    @Query("{ '$or': [ { 'requesterId': ?0, 'status': 'ACCEPTED' }, { 'recipientId': ?0, 'status': 'ACCEPTED' } ] }")
    List<Connection> findAllAccepted(String userId);
}
