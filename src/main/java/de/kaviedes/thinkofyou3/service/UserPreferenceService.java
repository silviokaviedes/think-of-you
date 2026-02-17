package de.kaviedes.thinkofyou3.service;

import de.kaviedes.thinkofyou3.dto.MoodOptionDTO;
import de.kaviedes.thinkofyou3.dto.UserMoodPreferencesDTO;
import de.kaviedes.thinkofyou3.model.Mood;
import de.kaviedes.thinkofyou3.model.User;
import de.kaviedes.thinkofyou3.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserPreferenceService {
    public static final int MAX_FAVORITE_MOODS = 8;

    private final UserRepository userRepository;

    public UserPreferenceService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserMoodPreferencesDTO getMoodPreferences(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> normalizedFavorites = normalizeFavoriteMoods(user.getFavoriteMoods());
        if (!normalizedFavorites.equals(user.getFavoriteMoods())) {
            user.setFavoriteMoods(normalizedFavorites);
            userRepository.save(user);
        }

        return new UserMoodPreferencesDTO(getAvailableMoods(), normalizedFavorites, MAX_FAVORITE_MOODS);
    }

    public List<String> updateFavoriteMoods(String username, List<String> favoriteMoods) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> normalized = normalizeFavoriteMoods(favoriteMoods);
        user.setFavoriteMoods(normalized);
        userRepository.save(user);
        return normalized;
    }

    private List<String> normalizeFavoriteMoods(List<String> favoriteMoods) {
        List<String> incoming = favoriteMoods == null ? Mood.defaultFavorites() : favoriteMoods;
        Set<String> allowedValues = Arrays.stream(Mood.values())
                .map(Mood::getValue)
                .collect(Collectors.toSet());

        LinkedHashSet<String> deduplicated = new LinkedHashSet<>();
        for (String moodValue : incoming) {
            if (moodValue == null || moodValue.isBlank()) {
                continue;
            }
            if (!allowedValues.contains(moodValue)) {
                throw new RuntimeException("Unknown mood: " + moodValue);
            }
            deduplicated.add(moodValue);
        }

        if (deduplicated.isEmpty()) {
            deduplicated.addAll(Mood.defaultFavorites());
        }
        if (deduplicated.size() > MAX_FAVORITE_MOODS) {
            throw new RuntimeException("You can select up to " + MAX_FAVORITE_MOODS + " favorite emojis");
        }
        return new ArrayList<>(deduplicated);
    }

    private List<MoodOptionDTO> getAvailableMoods() {
        return Arrays.stream(Mood.values())
                .map(mood -> new MoodOptionDTO(mood.getValue(), mood.getEmoji(), mood.getLabel()))
                .toList();
    }
}
