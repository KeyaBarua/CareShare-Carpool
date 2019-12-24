package com.example.careshare;

public class FareDetails {

    double distance, cost;
    int duration;

    public FareDetails() {
    }

    public FareDetails(double distance, double cost, int duration) {
        this.distance = distance;
        this.cost = cost;
        this.duration = duration;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
