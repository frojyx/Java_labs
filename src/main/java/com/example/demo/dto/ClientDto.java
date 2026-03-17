package com.example.demo.dto;

public class ClientDto {
    private Long id;

    private String firstName;

    private String lastName;

    // Конструктор по умолчанию (обязателен для библиотек маппинга)
    public ClientDto() {
        // Intentionally empty: required by frameworks (e.g., Jackson/MapStruct) for object instantiation.
    }

    // Геттеры и Сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
