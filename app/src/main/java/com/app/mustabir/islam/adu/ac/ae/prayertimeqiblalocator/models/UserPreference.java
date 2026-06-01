package com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.models;

public class UserPreference {
    private int id;
    private String country;
    private String city;
    private String calculationMethod;
    private boolean notificationEnabled;
    private int tasbihTarget;
    private String theme;
    private double latitude;
    private double longitude;

    public UserPreference() {
        country = "United Arab Emirates";
        city = "Abu Dhabi"; calculationMethod = "Muslim World League";
        notificationEnabled = true; tasbihTarget = 33;
        theme = "Light"; latitude = 24.4539; longitude = 54.3773;
    }

    public int getId()                    { return id; }
    public String getCountry()            { return country; }
    public String getCity()               { return city; }
    public String getCalculationMethod()  { return calculationMethod; }
    public boolean isNotificationEnabled(){ return notificationEnabled; }
    public int getTasbihTarget()          { return tasbihTarget; }
    public String getTheme()              { return theme; }
    public double getLatitude()           { return latitude; }
    public double getLongitude()          { return longitude; }

    public void setId(int id)                               { this.id = id; }
    public void setCountry(String country)                  { this.country = country; }
    public void setCity(String city)                        { this.city = city; }
    public void setCalculationMethod(String m)              { this.calculationMethod = m; }
    public void setNotificationEnabled(boolean n)           { this.notificationEnabled = n; }
    public void setTasbihTarget(int t)                      { this.tasbihTarget = t; }
    public void setTheme(String theme)                      { this.theme = theme; }
    public void setLatitude(double latitude)                { this.latitude = latitude; }
    public void setLongitude(double longitude)              { this.longitude = longitude; }
}
