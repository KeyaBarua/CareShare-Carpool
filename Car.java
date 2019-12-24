package com.example.careshare;

public class Car {

    private String name, brand, number, seats;

    public Car() {
    }

    public Car(String name, String brand, String number, String seats) {
        this.name = name;
        this.brand = brand;
        this.number = number;
        this.seats = seats;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getSeats() {
        return seats;
    }

    public void setSeats(String seats) {
        this.seats = seats;
    }
}
