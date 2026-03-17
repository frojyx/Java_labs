package com.example.demo.mapper;

import com.example.demo.dto.IngredientDto;
import com.example.demo.entity.Ingredient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class IngredientMapper {
    public IngredientDto toDto(Ingredient ingredient) {
        IngredientDto dto = new IngredientDto();
        dto.setId(ingredient.getId());
        dto.setName(ingredient.getName());
        return dto;
    }

    public Ingredient toEntity(IngredientDto ingredientDto) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(ingredientDto.getName());
        return ingredient;
    }

    public List<IngredientDto> toDtoList(List<Ingredient> ingredients) {
        List<IngredientDto> list = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            list.add(toDto(ingredient));
        }
        return list;
    }
}
