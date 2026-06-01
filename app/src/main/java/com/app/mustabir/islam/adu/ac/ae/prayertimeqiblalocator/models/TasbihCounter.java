package com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.models;

public class TasbihCounter {
    private int id;
    private String dhikrName;
    private int currentCount;
    private int targetCount;

    public TasbihCounter() {}

    public int getId()             { return id; }
    public String getDhikrName()   { return dhikrName; }
    public int getCurrentCount()   { return currentCount; }
    public int getTargetCount()    { return targetCount; }

    public void setId(int id)                     { this.id = id; }
    public void setDhikrName(String dhikrName)    { this.dhikrName = dhikrName; }
    public void setCurrentCount(int currentCount) { this.currentCount = currentCount; }
    public void setTargetCount(int targetCount)   { this.targetCount = targetCount; }
}
