package com.example.olio_viikko10;

public class CarData {
    private String type;
    private int amount;

    public CarData(String t, int a) {
        type = t;
        amount = a;
    }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public int getAmount() {
        return amount;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }
}
