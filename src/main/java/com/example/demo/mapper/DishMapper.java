package com.example.demo.mapper;

import com.example.demo.dto.DishDto;
import com.example.demo.entity.Dish;
import com.example.demo.entity.Ingredient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DishMapper {
    public DishDto toDto(Dish dish) {
        DishDto dishDto = new DishDto();
        dishDto.setId(dish.getId());
        dishDto.setName(dish.getName());
        dishDto.setCategory(dish.getCategory() != null ? dish.getCategory().getName() : null);
        dishDto.setPrice(dish.getPrice());
        dishDto.setWeight(dish.getWeight());
        if (dish.getIngredients() != null) {
            dishDto.setIngredients(dish.getIngredients().stream()
                .map(Ingredient::getName)
                .collect(Collectors.toList()));
        }
        return dishDto;
    }

    public Dish toEntity(DishDto dishDto) {
        Dish dish = new Dish();
        dish.setName(dishDto.getName());
        dish.setPrice(dishDto.getPrice());
        dish.setWeight(dishDto.getWeight());
        return dish;
    }

    public List<DishDto> toDtoList(List<Dish> dishes) {
        List<DishDto> dishesDto = new ArrayList<>();
        for (Dish dish : dishes) {
            DishDto dishDto = toDto(dish);
            dishesDto.add(dishDto);
        }
        return dishesDto;
    }
}
