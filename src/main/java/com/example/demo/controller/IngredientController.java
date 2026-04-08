package com.example.demo.controller;

import com.example.demo.dto.IngredientDto;
import com.example.demo.service.IngredientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/ingredients")
@Tag(name = "Ingredients", description = "Operations with ingredients")
public class IngredientController {
    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @GetMapping
    @Operation(summary = "Get all ingredients")
    public List<IngredientDto> getAllIngredients() {
        return ingredientService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ingredient by ID")
    public IngredientDto getIngredientById(@PathVariable @Positive(message = "ID must be greater than 0") Long id) {
        return ingredientService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create ingredient")
    public IngredientDto createIngredient(@Valid @RequestBody IngredientDto ingredientDto) {
        return ingredientService.save(ingredientDto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update ingredient")
    public IngredientDto updateIngredient(@PathVariable @Positive(message = "ID must be greater than 0") Long id,
                                          @Valid @RequestBody IngredientDto ingredientDto) {
        return ingredientService.update(id, ingredientDto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete ingredient")
    public void deleteIngredient(@PathVariable @Positive(message = "ID must be greater than 0") Long id) {
        ingredientService.deleteById(id);
    }
}
