package com.example.hydrationgarden.models;

public enum PlantType {
    ALOE("Aloe Vera"),
    CACTUS("Kaktus"),
    ROSE("Ruža"),
    ORCHID("Orhideja"),
    SIMPLE_FLOWER("Cvjetić"),
    LEAFY_PLANT("Zelena Biljka");

    private final String displayName;

    PlantType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}