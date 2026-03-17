package com.example.demo.dto;

public class CategoryDto {
    private Long id;

    private String name;

    public CategoryDto() {
        // Intentionally empty: required by frameworks (e.g., Jackson/MapStruct) for object instantiation.
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
