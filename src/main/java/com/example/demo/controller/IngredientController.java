package com.example.demo.controller;

import com.example.demo.dto.IngredientDto;
import com.example.demo.service.IngredientService;
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
@RequestMapping("/api/ingredients")
public class IngredientController {
    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @GetMapping
    public List<IngredientDto> getAllIngredients() {
        return ingredientService.findAll();
    }

    @GetMapping("/{id}")
    public IngredientDto getIngredientById(@PathVariable Long id) {
        return ingredientService.findById(id);
    }

    @PostMapping
    public IngredientDto createIngredient(@RequestBody IngredientDto ingredientDto) {
        return ingredientService.save(ingredientDto);
    }

    @PutMapping("/{id}")
    public IngredientDto updateIngredient(@PathVariable Long id, @RequestBody IngredientDto ingredientDto) {
        return ingredientService.update(id, ingredientDto);
    }

    @DeleteMapping("/{id}")
    public void deleteIngredient(@PathVariable Long id) {
        ingredientService.deleteById(id);
    }
}
