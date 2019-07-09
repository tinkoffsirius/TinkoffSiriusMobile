package com.anikinkirill.tinkoffsiriusmobile.models;

/**
 * CREATED BY ANIKINKIRILL
 */
public class Coordinate {

    private double latitude;
    private double latitudeInternal;
    private double longitude;
    private double longitudeInternal;
    private boolean polar;

    public Coordinate(double latitude, double latitudeInternal, double longitude, double longitudeInternal, boolean polar) {
        this.latitude = latitude;
        this.latitudeInternal = latitudeInternal;
        this.longitude = longitude;
        this.longitudeInternal = longitudeInternal;
        this.polar = polar;
    }

    public Coordinate(){}

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatitudeInternal() {
        return latitudeInternal;
    }

    public void setLatitudeInternal(double latitudeInternal) {
        this.latitudeInternal = latitudeInternal;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLongitudeInternal() {
        return longitudeInternal;
    }

    public void setLongitudeInternal(double longitudeInternal) {
        this.longitudeInternal = longitudeInternal;
    }

    public boolean getPolar() {
        return polar;
    }

    public void setPolar(boolean polar) {
        this.polar = polar;
    }
}
