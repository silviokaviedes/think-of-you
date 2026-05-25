package de.kaviedes.thinkofyou3.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RecoveryRateLimitService {
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private final Map<String, Deque<Instant>> attemptsByKey = new ConcurrentHashMap<>();

    public void check(String key) {
        Instant now = Instant.now();
        Deque<Instant> attempts = attemptsByKey.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        synchronized (attempts) {
            while (!attempts.isEmpty() && attempts.peekFirst().isBefore(now.minus(WINDOW))) {
                attempts.removeFirst();
            }
            if (attempts.size() >= MAX_ATTEMPTS) {
                throw new RuntimeException("Too many recovery attempts. Please try again later.");
            }
            attempts.addLast(now);
        }
    }
}
