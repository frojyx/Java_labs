package com.example.demo.mapper;

import com.example.demo.dto.DishDto;
import com.example.demo.entity.Dish;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DishMapper {

    public DishDto toDto(Dish dish) {
        DishDto dishDto = new DishDto();
        dishDto.setName(dish.getName());
        dishDto.setCategory(dish.getCategory());
        dishDto.setId(dish.getId());
        return dishDto;
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

