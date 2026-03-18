package com.example.demo.controller;

import com.example.demo.dto.DishDto;
import com.example.demo.service.DishService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DishController {
    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @PostMapping("/dish")
    public DishDto createDish(@RequestBody DishDto dishDto) {
        return dishService.save(dishDto);
    }

    @GetMapping("/dishAll")
    public List<DishDto> getAllDishes() {
        return dishService.findAll();
    }

    @GetMapping("/dish/search")
    public Page<DishDto> searchDishes(
        @RequestParam(required = false) String categoryName,
        @RequestParam(required = false) String ingredientName,
        @RequestParam(required = false) String namePart,
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(defaultValue = "false") boolean useNativeQuery,
        Pageable pageable
    ) {
        return dishService.searchWithFilters(categoryName, ingredientName, namePart, minPrice, maxPrice,
            pageable, useNativeQuery);
    }

    @GetMapping("/dish/{id}")
    public DishDto getIdByDish(@PathVariable Long id) {
        return dishService.findById(id);
    }

    @PutMapping("/dish/{id}")
    public DishDto updateDish(@PathVariable Long id, @RequestBody DishDto dishDto) {
        return dishService.update(id, dishDto);
    }

    @DeleteMapping("/dish/{id}")
    public void deleteDish(@PathVariable Long id) {
        dishService.deleteById(id);
    }
}
