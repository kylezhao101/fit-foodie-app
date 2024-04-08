package com.example.fitnessgachaapp;

public class GachaItem {
    private int id;
    private String name;
    private int spriteId;
    private int dupeCount;

    public GachaItem(int id, String name, int spriteId, int dupeCount) {
        this.id = id;
        this.name = name;
        this.spriteId = spriteId;
        this.dupeCount = dupeCount;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getSpriteId() {
        return spriteId;
    }

    public int getDupeCount() {
        return dupeCount;
    }
}
