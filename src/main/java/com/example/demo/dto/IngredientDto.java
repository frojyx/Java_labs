package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO ингредиента")
public class IngredientDto {
    @Schema(description = "Идентификатор ингредиента", example = "3")
    private Long id;

    @NotBlank(message = "Название ингредиента обязательно")
    @Size(max = 100, message = "Название ингредиента не должно превышать 100 символов")
    @Schema(description = "Название ингредиента", example = "Сыр")
    private String name;

    public IngredientDto() {
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
