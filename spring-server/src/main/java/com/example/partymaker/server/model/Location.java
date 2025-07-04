package com.example.partymaker.server.model;

public class Location {
    private double latitude;
    private double longitude;
    private String address;

    public Location() {
        // Constructor ריק נדרש עבור Firebase
    }

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
} 