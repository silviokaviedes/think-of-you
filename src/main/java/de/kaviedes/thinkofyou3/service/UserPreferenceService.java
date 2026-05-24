package de.kaviedes.thinkofyou3.service;

import de.kaviedes.thinkofyou3.dto.DashboardPreferenceDTO;
import de.kaviedes.thinkofyou3.dto.EnergyLevelsDTO;
import de.kaviedes.thinkofyou3.dto.MoodOptionDTO;
import de.kaviedes.thinkofyou3.dto.UserMoodPreferencesDTO;
import de.kaviedes.thinkofyou3.model.DashboardDisplayMode;
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
    public static final int DEFAULT_ENERGY_LEVEL = 50;

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

    public DashboardPreferenceDTO getDashboardPreference(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DashboardDisplayMode mode = normalizeDashboardMode(user.getDashboardDisplayMode());
        if (user.getDashboardDisplayMode() != mode) {
            user.setDashboardDisplayMode(mode);
            userRepository.save(user);
        }

        return new DashboardPreferenceDTO(mode.getValue());
    }

    public DashboardPreferenceDTO updateDashboardPreference(String username, String modeValue) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DashboardDisplayMode mode = DashboardDisplayMode.fromValue(modeValue);
        user.setDashboardDisplayMode(mode);
        userRepository.save(user);
        return new DashboardPreferenceDTO(mode.getValue());
    }

    public EnergyLevelsDTO getEnergyLevels(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        EnergyLevelsDTO levels = normalizeEnergyLevels(user);
        if (!levels.getBody().equals(user.getBodyEnergy())
                || !levels.getMind().equals(user.getMindEnergy())
                || !levels.getHeart().equals(user.getHeartEnergy())) {
            applyEnergyLevels(user, levels);
            userRepository.save(user);
        }
        return levels;
    }

    public EnergyLevelsDTO updateEnergyLevels(String username, EnergyLevelsDTO request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        EnergyLevelsDTO levels = validateEnergyLevels(request);
        applyEnergyLevels(user, levels);
        userRepository.save(user);
        return levels;
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

    private DashboardDisplayMode normalizeDashboardMode(DashboardDisplayMode mode) {
        return mode == null ? DashboardDisplayMode.COUNTS : mode;
    }

    private EnergyLevelsDTO normalizeEnergyLevels(User user) {
        return new EnergyLevelsDTO(
                normalizeEnergyLevel(user.getBodyEnergy()),
                normalizeEnergyLevel(user.getMindEnergy()),
                normalizeEnergyLevel(user.getHeartEnergy())
        );
    }

    private EnergyLevelsDTO validateEnergyLevels(EnergyLevelsDTO request) {
        if (request == null) {
            throw new RuntimeException("Energy levels are required");
        }
        return new EnergyLevelsDTO(
                validateEnergyLevel("body", request.getBody()),
                validateEnergyLevel("mind", request.getMind()),
                validateEnergyLevel("heart", request.getHeart())
        );
    }

    private int normalizeEnergyLevel(Integer value) {
        return value == null ? DEFAULT_ENERGY_LEVEL : validateEnergyLevel("energy", value);
    }

    private int validateEnergyLevel(String name, Integer value) {
        if (value == null) {
            throw new RuntimeException("Energy level " + name + " is required");
        }
        if (value < 0 || value > 100) {
            throw new RuntimeException("Energy level " + name + " must be between 0 and 100");
        }
        return value;
    }

    private void applyEnergyLevels(User user, EnergyLevelsDTO levels) {
        user.setBodyEnergy(levels.getBody());
        user.setMindEnergy(levels.getMind());
        user.setHeartEnergy(levels.getHeart());
    }
}
