package com.example.demo.repository;

import com.example.demo.entity.Dish;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class DishRepository {

    private static final String CATEGORY_SOUPS = "SOUPS";

    private static final String CATEGORY_MAINS = "MAINS";

    private static final String CATEGORY_SALADS = "SALADS";

    private static final String CATEGORY_DESSERTS = "DESSERTS";

    private final List<Dish> dishes = new ArrayList<>();

    public DishRepository() {
        // Наполняем меню данными, используя созданные константы
        dishes.add(new Dish("Борщ", CATEGORY_SOUPS, 350.0, 1, 400));
        dishes.add(new Dish("Стейк Рибай", CATEGORY_MAINS, 1200.0, 2, 350));
        dishes.add(new Dish("Цезарь", CATEGORY_SALADS, 450.0, 3, 250));
        dishes.add(new Dish("Том Ям", CATEGORY_SOUPS, 550.0, 4, 450));
        dishes.add(new Dish("Тирамису", CATEGORY_DESSERTS, 300.0, 5, 150));
        dishes.add(new Dish("Паста Карбонара", CATEGORY_MAINS, 480.0, 6, 300));
        dishes.add(new Dish("Медовик", CATEGORY_DESSERTS, 250.0, 7, 120));
        dishes.add(new Dish("Греческий салат", CATEGORY_SALADS, 380.0, 8, 200));
        dishes.add(new Dish("Куриный бульон", CATEGORY_SOUPS, 200.0, 9, 300));
        dishes.add(new Dish("Бургер", CATEGORY_MAINS, 600.0, 10, 450));
    }

    public List<Dish> findByCategory(String category) {
        List<Dish> result = new ArrayList<>();
        for (Dish current : dishes) {
            if (current.getCategory().equalsIgnoreCase(category)) {
                result.add(current);
            }
        }
        return result;
    }

    public List<Dish> findByPrice(double price) {
        List<Dish> result = new ArrayList<>();
        for (Dish current : dishes) {
            if (current.getPrice() == price) {
                result.add(current);
            }
        }
        return result;
    }
}
