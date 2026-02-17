package de.kaviedes.thinkofyou3.dto;

public class MoodOptionDTO {
    private String value;
    private String emoji;
    private String label;

    public MoodOptionDTO() {
    }

    public MoodOptionDTO(String value, String emoji, String label) {
        this.value = value;
        this.emoji = emoji;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
