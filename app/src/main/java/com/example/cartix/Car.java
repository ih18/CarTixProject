package com.example.cartix;

public class Car {

    private String carName;
    private int engineVolume;
    private int horsePower;
    private String carType;
    private String year;
    private String money;
    private boolean favorite; // New field to track favorite status
    private String id; // Unique identifier for the car

    public Car(String carName, int engineVolume, int horsePower, String carType, String year, String money) {
        this.engineVolume = engineVolume;
        this.horsePower = horsePower;
        this.carType = carType;
        this.year = year;
        this.money = money;
        this.carName = carName;
        this.favorite = false;
        this.id = generateId(); // Generate unique ID
    }

    public Car(String carName, String year, String money) {
        this.carName = carName;
        this.year = year;
        this.money = money;
        this.favorite = false;
        this.id = generateId(); // Generate unique ID
    }

    // Helper method to generate a unique ID
    private String generateId() {
        return carName + "_" + year + "_" + System.currentTimeMillis();
    }

    public String getCarName() {
        return carName;
    }

    public int getEngineVolume() {
        return engineVolume;
    }

    public int getHorsePower() {
        return horsePower;
    }

    public String getCarType() {
        return carType;
    }

    public String getYear() {
        return year;
    }

    public String getMoney() {
        return money;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // For equals and hashCode, we'll use the ID to determine equality
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Car car = (Car) obj;
        return id.equals(car.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}