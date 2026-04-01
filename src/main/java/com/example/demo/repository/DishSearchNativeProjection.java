package com.example.demo.repository;

public interface DishSearchNativeProjection {
    Long getId();

    String getName();

    double getPrice();

    int getWeight();

    String getCategory();

    String getIngredientsCsv();
}
