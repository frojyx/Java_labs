package com.example.demo.service;

import com.example.demo.dto.IngredientDto;
import com.example.demo.entity.Dish;
import com.example.demo.entity.Ingredient;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.IngredientMapper;
import com.example.demo.repository.IngredientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class IngredientService {
    private static final String INGREDIENT_WITH_ID_PREFIX = "Ингредиент с ID ";

    private static final String NOT_FOUND_SUFFIX = " не найден";

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
            .orElseThrow(() -> new ResourceNotFoundException(INGREDIENT_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX));
    }

    @Transactional
    public IngredientDto save(IngredientDto ingredientDto) {
        if (ingredientRepository.findByName(ingredientDto.getName()).isPresent()) {
            throw new ConflictException("Ингредиент с названием '" + ingredientDto.getName() + "' уже существует");
        }

        Ingredient ingredient = ingredientMapper.toEntity(ingredientDto);
        Ingredient savedIngredient = ingredientRepository.save(ingredient);
        dishService.invalidateSearchCache();
        return ingredientMapper.toDto(savedIngredient);
    }

    @Transactional
    public IngredientDto update(Long id, IngredientDto ingredientDto) {
        Ingredient ingredient = ingredientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(INGREDIENT_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX));

        ingredientRepository.findByName(ingredientDto.getName())
            .filter(existingIngredient -> !existingIngredient.getId().equals(id))
            .ifPresent(existingIngredient -> {
                throw new ConflictException(
                    "Ингредиент с названием '" + ingredientDto.getName() + "' уже существует"
                );
            });

        ingredient.setName(ingredientDto.getName());
        Ingredient updatedIngredient = ingredientRepository.save(ingredient);
        dishService.invalidateSearchCache();
        return ingredientMapper.toDto(updatedIngredient);
    }

    @Transactional
    public void deleteById(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(INGREDIENT_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX));

        if (ingredient.getDishes() != null) {
            for (Dish dish : new ArrayList<>(ingredient.getDishes())) {
                dishService.deleteById(dish.getId());
            }
        }

        ingredientRepository.delete(ingredient);
        dishService.invalidateSearchCache();
    }
}
