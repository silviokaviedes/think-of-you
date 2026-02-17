package de.kaviedes.thinkofyou3.dto;

import java.util.List;

public class UpdateFavoriteMoodsRequest {
    private List<String> favoriteMoods;

    public List<String> getFavoriteMoods() {
        return favoriteMoods;
    }

    public void setFavoriteMoods(List<String> favoriteMoods) {
        this.favoriteMoods = favoriteMoods;
    }
}
