package com.example.hydrationgarden.models;

import com.example.hydrationgarden.R;

public class Plant {
    private String id;
    private String name;
    private PlantType type;
    private String happyMessage;
    private String sadMessage;
    private boolean isHappy;
    private int happyImageResId;
    private int sadImageResId;

    public Plant() {
    }

    public Plant(String id, String name, PlantType type) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.isHappy = true;
        setMessagesAndImages();
    }

    private void setMessagesAndImages() {
        switch (type) {
            case ALOE:
                happyMessage = "Mmmm, i ja sam puna vode!";
                sadMessage = "Mene si zaboravio kao i vodu...";
                happyImageResId = R.drawable.plant_happy_aloe;
                sadImageResId = R.drawable.plant_sad_aloe;
                break;

            case CACTUS:
                happyMessage = "Ja mogu preživjeti, ali ti trebaš vodu!";
                sadMessage = "Čak i ja trebam vodu ponekad...";
                happyImageResId = R.drawable.plant_happy_cactus;
                sadImageResId = R.drawable.plant_sad_cactus;
                break;

            case ROSE:
                happyMessage = "Kao ruža u cvatu, i ti cvjetaš s vodom!";
                sadMessage = "Vene mi latice bez vode...";
                happyImageResId = R.drawable.plant_happy_rose;
                sadImageResId = R.drawable.plant_sad_rose;
                break;

            case ORCHID:
                happyMessage = "Elegantan si kao ja kad piješ dovoljno vode!";
                sadMessage = "Potrebna mi je vlaga... kao i tebi!";
                happyImageResId = R.drawable.plant_happy_orchid;
                sadImageResId = R.drawable.plant_sad_orchid;
                break;

            case SIMPLE_FLOWER:
                happyMessage = "Cvjetam kada i ti cvjetaš!";
                sadMessage = "Veni mi se bez vode...";
                happyImageResId = R.drawable.plant_happy_flower;
                sadImageResId = R.drawable.plant_sad_flower;
                break;

            case LEAFY_PLANT:
                happyMessage = "Zeleno i svježe, kao što i ti trebaš biti!";
                sadMessage = "Žute mi se listovi... kao i tebi koža bez vode!";
                happyImageResId = R.drawable.plant_happy_leafy;
                sadImageResId = R.drawable.plant_sad_leafy;
                break;

            default:
                // Fallback case
                happyMessage = "Sretna sam!";
                sadMessage = "Trebam vodu...";
                happyImageResId = R.drawable.plant_happy_aloe; // Default image
                sadImageResId = R.drawable.plant_sad_aloe; // Default image
                break;
        }
    }

    public String getCurrentMessage() {
        return isHappy ? happyMessage : sadMessage;
    }

    public int getCurrentImageResId() {
        return isHappy ? happyImageResId : sadImageResId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public PlantType getType() { return type; }
    public void setType(PlantType type) {
        this.type = type;
        setMessagesAndImages();
    }

    public String getHappyMessage() { return happyMessage; }
    public void setHappyMessage(String happyMessage) { this.happyMessage = happyMessage; }

    public String getSadMessage() { return sadMessage; }
    public void setSadMessage(String sadMessage) { this.sadMessage = sadMessage; }

    public boolean isHappy() { return isHappy; }
    public void setHappy(boolean happy) { isHappy = happy; }

    public int getHappyImageResId() { return happyImageResId; }
    public void setHappyImageResId(int happyImageResId) { this.happyImageResId = happyImageResId; }

    public int getSadImageResId() { return sadImageResId; }
    public void setSadImageResId(int sadImageResId) { this.sadImageResId = sadImageResId; }
}