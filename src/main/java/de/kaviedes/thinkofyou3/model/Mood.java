package de.kaviedes.thinkofyou3.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Mood {
    HAPPY("happy", "😊"),
    SAD("sad", "😢"),
    ANGRY("angry", "😠"),
    LOVE("love", "❤️"),
    EXCITED("excited", "🤗"),
    WORRIED("worried", "😟"),
    GRATEFUL("grateful", "🙏"),
    NONE("none", "💭");

    private final String value;
    private final String emoji;

    Mood(String value, String emoji) {
        this.value = value;
        this.emoji = emoji;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getEmoji() {
        return emoji;
    }

    @JsonCreator
    public static Mood fromValue(String value) {
        for (Mood mood : Mood.values()) {
            if (mood.getValue().equals(value)) {
                return mood;
            }
        }
        return NONE;
    }
}
