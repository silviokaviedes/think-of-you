package de.kaviedes.thinkofyou3.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

public enum Mood {
    HAPPY("happy", "\uD83D\uDE0A", "Happy"),
    SAD("sad", "\uD83D\uDE22", "Sad"),
    ANGRY("angry", "\uD83D\uDE20", "Angry"),
    LOVE("love", "\u2764\uFE0F", "Love"),
    EXCITED("excited", "\uD83E\uDD73", "Excited"),
    WORRIED("worried", "\uD83D\uDE1F", "Worried"),
    GRATEFUL("grateful", "\uD83D\uDE4F", "Grateful"),
    NONE("none", "\uD83D\uDCAD", "Neutral"),
    HUG("hug", "\uD83E\uDD17", "Hug"),
    EXHAUSTED("exhausted", "\uD83D\uDE2E\u200D\uD83D\uDCA8", "Exhausted"),
    CALM("calm", "\uD83D\uDE0C", "Calm"),
    PLAYFUL("playful", "\uD83D\uDE1C", "Playful"),
    CONFUSED("confused", "\uD83D\uDE15", "Confused"),
    PROUD("proud", "\uD83D\uDE0E", "Proud"),
    SHY("shy", "\uD83E\uDD7A", "Shy"),
    SICK("sick", "\uD83E\uDD12", "Sick"),
    STRESSED("stressed", "\uD83D\uDE35", "Stressed"),
    HOPEFUL("hopeful", "\uD83C\uDF1F", "Hopeful"),
    CELEBRATING("celebrating", "\uD83C\uDF89", "Celebrating"),
    LONELY("lonely", "\uD83D\uDE14", "Lonely");

    private final String value;
    private final String emoji;
    private final String label;

    Mood(String value, String emoji, String label) {
        this.value = value;
        this.emoji = emoji;
        this.label = label;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getLabel() {
        return label;
    }

    public static List<String> defaultFavorites() {
        return List.of(
                LOVE.getValue(),
                HUG.getValue(),
                HAPPY.getValue(),
                GRATEFUL.getValue(),
                EXCITED.getValue(),
                CALM.getValue(),
                WORRIED.getValue(),
                NONE.getValue()
        );
    }

    @JsonCreator
    public static Mood fromValue(String value) {
        if (value == null || value.isBlank()) {
            return NONE;
        }
        for (Mood mood : Mood.values()) {
            if (mood.getValue().equals(value)) {
                return mood;
            }
        }
        return NONE;
    }
}
