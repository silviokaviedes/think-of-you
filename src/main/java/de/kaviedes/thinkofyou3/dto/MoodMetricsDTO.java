package de.kaviedes.thinkofyou3.dto;

import de.kaviedes.thinkofyou3.model.Mood;
import java.util.Map;

public class MoodMetricsDTO {
    private Map<String, Map<Mood, Long>> timeBuckets;
    private Map<Mood, Long> totalMoodDistribution;

    public MoodMetricsDTO() {}

    public MoodMetricsDTO(Map<String, Map<Mood, Long>> timeBuckets, Map<Mood, Long> totalMoodDistribution) {
        this.timeBuckets = timeBuckets;
        this.totalMoodDistribution = totalMoodDistribution;
    }

    public Map<String, Map<Mood, Long>> getTimeBuckets() {
        return timeBuckets;
    }

    public void setTimeBuckets(Map<String, Map<Mood, Long>> timeBuckets) {
        this.timeBuckets = timeBuckets;
    }

    public Map<Mood, Long> getTotalMoodDistribution() {
        return totalMoodDistribution;
    }

    public void setTotalMoodDistribution(Map<Mood, Long> totalMoodDistribution) {
        this.totalMoodDistribution = totalMoodDistribution;
    }
}
