package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public class OrderDto {
    @JsonIgnore
    private Long id;

    private String clientFirstName;

    private String clientLastName;

    private List<String> dishNames;

    public OrderDto() {
    }

    // Геттеры и Сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientFirstName() {
        return clientFirstName;
    }

    public void setClientFirstName(String clientFirstName) {
        this.clientFirstName = clientFirstName;
    }

    public String getClientLastName() {
        return clientLastName;
    }

    public void setClientLastName(String clientLastName) {
        this.clientLastName = clientLastName;
    }

    public List<String> getDishNames() {
        return dishNames;
    }

    public void setDishNames(List<String> dishNames) {
        this.dishNames = dishNames;
    }
}
