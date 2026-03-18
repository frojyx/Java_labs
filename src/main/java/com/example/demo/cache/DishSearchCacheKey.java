package com.example.demo.cache;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DishSearchCacheKey {
    private final boolean nativeQuery;

    private final String categoryName;

    private final String ingredientName;

    private final String namePart;

    private final Double minPrice;

    private final Double maxPrice;

    private final int page;

    private final int size;

    private final List<String> sortOrders;

    public DishSearchCacheKey(boolean nativeQuery, String categoryName, String ingredientName, String namePart,
                              Double minPrice, Double maxPrice, int page, int size, Sort sort) {
        this.nativeQuery = nativeQuery;
        this.categoryName = normalize(categoryName);
        this.ingredientName = normalize(ingredientName);
        this.namePart = normalize(namePart);
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.page = page;
        this.size = size;
        this.sortOrders = buildSortOrders(sort);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    private List<String> buildSortOrders(Sort sort) {
        List<String> orders = new ArrayList<>();
        for (Sort.Order order : sort) {
            orders.add(order.getProperty() + ":" + order.getDirection().name());
        }
        return List.copyOf(orders);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DishSearchCacheKey that)) {
            return false;
        }
        return nativeQuery == that.nativeQuery
            && page == that.page
            && size == that.size
            && Objects.equals(categoryName, that.categoryName)
            && Objects.equals(ingredientName, that.ingredientName)
            && Objects.equals(namePart, that.namePart)
            && Objects.equals(minPrice, that.minPrice)
            && Objects.equals(maxPrice, that.maxPrice)
            && Objects.equals(sortOrders, that.sortOrders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nativeQuery, categoryName, ingredientName, namePart, minPrice, maxPrice, page, size,
            sortOrders);
    }

    @Override
    public String toString() {
        return "DishSearchCacheKey{"
            + "nativeQuery=" + nativeQuery
            + ", categoryName='" + categoryName + '\''
            + ", ingredientName='" + ingredientName + '\''
            + ", namePart='" + namePart + '\''
            + ", minPrice=" + minPrice
            + ", maxPrice=" + maxPrice
            + ", page=" + page
            + ", size=" + size
            + ", sortOrders=" + sortOrders
            + '}';
    }
}

