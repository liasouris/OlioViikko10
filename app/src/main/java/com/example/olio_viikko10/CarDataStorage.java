package com.example.olio_viikko10;

import java.util.ArrayList;

public class CarDataStorage {
    private static CarDataStorage instance;
    private String city;
    private int year;
    private ArrayList<CarData> carDataList = new ArrayList<>();

    private CarDataStorage() {
        carDataList = new ArrayList<>();
    }

    public static CarDataStorage getInstance() {
        if (instance == null) {
            instance = new CarDataStorage();
        }
        return instance;
    }

    public ArrayList<CarData> getCarData() {
        return carDataList;
    }

    public void addCarData(CarData data) {
        carDataList.add(data);
    }

    public void clearData() {
        carDataList.clear();
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getYear() {
        return year;
    }
}

