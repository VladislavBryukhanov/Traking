package com.example.nameless.location;

/**
 * Created by nameless on 30.03.18.
 */

public class CustomLatLng {

    private double latitude;
    private double longitude;

    public CustomLatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public CustomLatLng() {}

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
