package com.example.demo.controller;

import com.example.demo.dto.DishDto;
import com.example.demo.service.DishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
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
import java.util.Map;

@RestController
@Validated
@RequestMapping("/api")
@Tag(name = "Dishes", description = "Operations with dishes")
public class DishController {
    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @PostMapping("/dish")
    @Operation(summary = "Create dish")
    public DishDto createDish(@Valid @RequestBody DishDto dishDto) {
        return dishService.save(dishDto);
    }

    @GetMapping("/dishAll")
    @Operation(summary = "Get all dishes")
    public List<DishDto> getAllDishes() {
        return dishService.findAll();
    }

    @GetMapping("/dish/search")
    @Operation(summary = "Search dishes with filters")
    public Page<DishDto> searchDishes(
        @Parameter(description = "Category name")
        @RequestParam(required = false) String categoryName,
        @Parameter(description = "Ingredient name")
        @RequestParam(required = false) String ingredientName,
        @Parameter(description = "Part of dish name")
        @RequestParam(required = false) String namePart,
        @DecimalMin(value = "0.0", message = "Min price must be non-negative")
        @Parameter(description = "Minimum price")
        @RequestParam(required = false) Double minPrice,
        @DecimalMin(value = "0.0", message = "Max price must be non-negative")
        @Parameter(description = "Maximum price")
        @RequestParam(required = false) Double maxPrice,
        @Parameter(description = "Use native query")
        @RequestParam(defaultValue = "false") boolean useNativeQuery,
        @PositiveOrZero(message = "Page number must be non-negative")
        @Parameter(description = "Page number", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Min(value = 1, message = "Page size must be at least 1")
        @Max(value = 100, message = "Page size must be at most 100")
        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") int size,
        Pageable pageable
    ) {
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by("id").ascending();
        Pageable effectivePageable = PageRequest.of(page, size, sort);
        return dishService.searchWithFilters(categoryName, ingredientName, namePart, minPrice, maxPrice,
            effectivePageable, useNativeQuery);
    }

    @GetMapping("/dish/{id}")
    @Operation(summary = "Get dish by ID")
    public DishDto getIdByDish(@PathVariable @Positive(message = "ID must be greater than 0") Long id) {
        return dishService.findById(id);
    }

    @PutMapping("/dish/{id}")
    @Operation(summary = "Update dish")
    public DishDto updateDish(@PathVariable @Positive(message = "ID must be greater than 0") Long id,
                              @Valid @RequestBody DishDto dishDto) {
        return dishService.update(id, dishDto);
    }

    @DeleteMapping("/dish/{id}")
    @Operation(summary = "Delete dish")
    public void deleteDish(@PathVariable @Positive(message = "ID must be greater than 0") Long id) {
        dishService.deleteById(id);
    }

    @GetMapping("/dish/cache/status")
    @Operation(summary = "Get dish search cache status")
    public Map<String, Object> getDishCacheStatus() {
        return dishService.getCacheStatus();
    }
}
