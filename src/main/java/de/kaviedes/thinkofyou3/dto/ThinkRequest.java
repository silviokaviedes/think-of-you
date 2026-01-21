package de.kaviedes.thinkofyou3.dto;

public class ThinkRequest {
    private String mood;

    public ThinkRequest() {}

    public ThinkRequest(String mood) {
        this.mood = mood;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }
}
