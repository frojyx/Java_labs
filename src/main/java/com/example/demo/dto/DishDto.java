package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "DTO блюда")
public class DishDto {
    @Schema(description = "Идентификатор блюда", example = "5")
    private Long id;

    @DecimalMin(value = "0.0", inclusive = false, message = "Цена должна быть больше 0")
    @Schema(description = "Цена блюда", example = "18.50")
    private double price;

    @NotBlank(message = "Категория блюда обязательна")
    @Size(max = 100, message = "Название категории не должно превышать 100 символов")
    @Schema(description = "Название категории блюда", example = "Паста")
    private String category;

    @NotBlank(message = "Название блюда обязательно")
    @Size(max = 150, message = "Название блюда не должно превышать 150 символов")
    @Schema(description = "Название блюда", example = "Карбонара")
    private String name;

    @Positive(message = "Вес должен быть больше 0")
    @Schema(description = "Вес блюда в граммах", example = "350")
    private int weight;

    @NotEmpty(message = "Список ингредиентов не должен быть пустым")
    @ArraySchema(schema = @Schema(description = "Название ингредиента", example = "Бекон"))
    private List<String> ingredients;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
