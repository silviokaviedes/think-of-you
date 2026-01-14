package de.kaviedes.thinkofyou3.repository;

import de.kaviedes.thinkofyou3.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
}
