package de.kaviedes.thinkofyou3.dto;

import java.util.List;

public class UserMoodPreferencesDTO {
    private List<MoodOptionDTO> availableMoods;
    private List<String> favoriteMoods;
    private int maxFavorites;

    public UserMoodPreferencesDTO() {
    }

    public UserMoodPreferencesDTO(List<MoodOptionDTO> availableMoods, List<String> favoriteMoods, int maxFavorites) {
        this.availableMoods = availableMoods;
        this.favoriteMoods = favoriteMoods;
        this.maxFavorites = maxFavorites;
    }

    public List<MoodOptionDTO> getAvailableMoods() {
        return availableMoods;
    }

    public void setAvailableMoods(List<MoodOptionDTO> availableMoods) {
        this.availableMoods = availableMoods;
    }

    public List<String> getFavoriteMoods() {
        return favoriteMoods;
    }

    public void setFavoriteMoods(List<String> favoriteMoods) {
        this.favoriteMoods = favoriteMoods;
    }

    public int getMaxFavorites() {
        return maxFavorites;
    }

    public void setMaxFavorites(int maxFavorites) {
        this.maxFavorites = maxFavorites;
    }
}
