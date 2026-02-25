package com.example.demo.entity;

public class Dish {
    private final int id;

    private final String name;

    private final String category;

    private final int weight;

    private final double price;

    public Dish(String name, String category, double price, int id, int weight) {
        this.name = name;
        this.category = category;
        this.id = id;
        this.weight = weight;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public int getWeight() {
        return weight;
    }
}
