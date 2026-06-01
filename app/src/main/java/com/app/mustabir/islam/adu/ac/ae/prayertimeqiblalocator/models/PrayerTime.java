package com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.models;

public class PrayerTime {
    private String name;
    private String time;
    private boolean isNext;

    public PrayerTime(String name, String time) {
        this.name = name; this.time = time; this.isNext = false;
    }

    public String getName()       { return name; }
    public String getTime()       { return time; }
    public boolean isNext()       { return isNext; }
    public void setNext(boolean n){ this.isNext = n; }
}
