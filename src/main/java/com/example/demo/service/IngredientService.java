package com.example.demo.service;

import com.example.demo.dto.IngredientDto;
import com.example.demo.entity.Dish;
import com.example.demo.entity.Ingredient;
import com.example.demo.mapper.IngredientMapper;
import com.example.demo.repository.IngredientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class IngredientService {
    private final IngredientRepository ingredientRepository;

    private final IngredientMapper ingredientMapper;

    private final DishService dishService;

    public IngredientService(IngredientRepository ingredientRepository, IngredientMapper ingredientMapper,
                             DishService dishService) {
        this.ingredientRepository = ingredientRepository;
        this.ingredientMapper = ingredientMapper;
        this.dishService = dishService;
    }

    @Transactional(readOnly = true)
    public List<IngredientDto> findAll() {
        return ingredientMapper.toDtoList(ingredientRepository.findAll());
    }

    @Transactional(readOnly = true)
    public IngredientDto findById(Long id) {
        return ingredientRepository.findById(id)
            .map(ingredientMapper::toDto)
            .orElseThrow(() -> new RuntimeException("Ингредиент с ID " + id + " не найден"));
    }

    @Transactional
    public IngredientDto save(IngredientDto ingredientDto) {
        Ingredient ingredient = ingredientMapper.toEntity(ingredientDto);
        return ingredientMapper.toDto(ingredientRepository.save(ingredient));
    }

    @Transactional
    public IngredientDto update(Long id, IngredientDto ingredientDto) {
        Ingredient ingredient = ingredientRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ингредиент с ID " + id + " не найден"));
        ingredient.setName(ingredientDto.getName());
        return ingredientMapper.toDto(ingredientRepository.save(ingredient));
    }

    @Transactional
    public void deleteById(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ингредиент с ID " + id + " не найден"));

        if (ingredient.getDishes() != null) {
            for (Dish dish : new ArrayList<>(ingredient.getDishes())) {
                dishService.deleteById(dish.getId());
            }
        }

        ingredientRepository.delete(ingredient);
    }
}