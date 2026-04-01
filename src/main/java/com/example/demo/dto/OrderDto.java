package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "DTO заказа")
public class OrderDto {
    @JsonIgnore
    @Schema(description = "Идентификатор заказа", example = "15")
    private Long id;

    @NotBlank(message = "Имя клиента обязательно")
    @Size(max = 100, message = "Имя клиента не должно превышать 100 символов")
    @Schema(description = "Имя клиента", example = "Анна")
    private String clientFirstName;

    @NotBlank(message = "Фамилия клиента обязательна")
    @Size(max = 100, message = "Фамилия клиента не должна превышать 100 символов")
    @Schema(description = "Фамилия клиента", example = "Сидорова")
    private String clientLastName;

    @NotEmpty(message = "Список блюд не должен быть пустым")
    @ArraySchema(schema = @Schema(description = "Название блюда", example = "Карбонара"))
    private List<String> dishNames;

    public OrderDto() {
    }

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
