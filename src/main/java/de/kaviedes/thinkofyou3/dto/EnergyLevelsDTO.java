package de.kaviedes.thinkofyou3.dto;

public class EnergyLevelsDTO {
    private Integer body;
    private Integer mind;
    private Integer heart;

    public EnergyLevelsDTO() {
    }

    public EnergyLevelsDTO(Integer body, Integer mind, Integer heart) {
        this.body = body;
        this.mind = mind;
        this.heart = heart;
    }

    public Integer getBody() {
        return body;
    }

    public void setBody(Integer body) {
        this.body = body;
    }

    public Integer getMind() {
        return mind;
    }

    public void setMind(Integer mind) {
        this.mind = mind;
    }

    public Integer getHeart() {
        return heart;
    }

    public void setHeart(Integer heart) {
        this.heart = heart;
    }
}
