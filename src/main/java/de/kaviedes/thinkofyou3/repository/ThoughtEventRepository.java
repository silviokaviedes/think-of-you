package de.kaviedes.thinkofyou3.repository;

import de.kaviedes.thinkofyou3.model.ThoughtEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.Instant;
import java.util.List;

public interface ThoughtEventRepository extends MongoRepository<ThoughtEvent, String> {
    List<ThoughtEvent> findByConnectionIdAndSenderIdAndOccurredAtBetween(String connectionId, String senderId, Instant start, Instant end);
    List<ThoughtEvent> findByConnectionIdAndRecipientIdAndOccurredAtBetween(String connectionId, String recipientId, Instant start, Instant end);
}
