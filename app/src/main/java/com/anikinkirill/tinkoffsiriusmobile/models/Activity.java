package com.anikinkirill.tinkoffsiriusmobile.models;

/**
 * CREATED BY ANIKINKIRILL
 */
public class Activity {

    private int arrivalTimeSeconds;
    private Coordinate coordinates;
    private int dueTimeSeconds;
    private int id;
    private int readyTimeSeconds;

    public Activity(int arrivalTimeSeconds, Coordinate coordinates, int dueTimeSeconds, int id, int readyTimeSeconds) {
        this.arrivalTimeSeconds = arrivalTimeSeconds;
        this.coordinates = coordinates;
        this.dueTimeSeconds = dueTimeSeconds;
        this.id = id;
        this.readyTimeSeconds = readyTimeSeconds;
    }

    public Activity() {}

    public int getArrivalTimeSeconds() {
        return arrivalTimeSeconds;
    }

    public void setArrivalTimeSeconds(int arrivalTimeSeconds) {
        this.arrivalTimeSeconds = arrivalTimeSeconds;
    }

    public Coordinate getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinate coordinates) {
        this.coordinates = coordinates;
    }

    public int getDueTimeSeconds() {
        return dueTimeSeconds;
    }

    public void setDueTimeSeconds(int dueTimeSeconds) {
        this.dueTimeSeconds = dueTimeSeconds;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReadyTimeSeconds() {
        return readyTimeSeconds;
    }

    public void setReadyTimeSeconds(int readyTimeSeconds) {
        this.readyTimeSeconds = readyTimeSeconds;
    }
}
