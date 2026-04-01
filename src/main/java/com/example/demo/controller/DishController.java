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
@Tag(name = "Dishes", description = "Операции с блюдами")
public class DishController {
    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @PostMapping("/dish")
    @Operation(summary = "Создать блюдо")
    public DishDto createDish(@Valid @RequestBody DishDto dishDto) {
        return dishService.save(dishDto);
    }

    @GetMapping("/dishAll")
    @Operation(summary = "Получить все блюда")
    public List<DishDto> getAllDishes() {
        return dishService.findAll();
    }

    @GetMapping("/dish/search")
    @Operation(summary = "Поиск блюд по фильтрам")
    public Page<DishDto> searchDishes(
        @Parameter(description = "Название категории") 
        @RequestParam(required = false) String categoryName,
        @Parameter(description = "Название ингредиента")
        @RequestParam(required = false) String ingredientName,
        @Parameter(description = "Часть названия блюда")
        @RequestParam(required = false) String namePart,
        @DecimalMin(value = "0.0", message = "Минимальная цена не может быть отрицательной")
        @Parameter(description = "Минимальная цена")
        @RequestParam(required = false) Double minPrice,
        @DecimalMin(value = "0.0", message = "Максимальная цена не может быть отрицательной")
        @Parameter(description = "Максимальная цена")
        @RequestParam(required = false) Double maxPrice,
        @Parameter(description = "Использовать native query")
        @RequestParam(defaultValue = "false") boolean useNativeQuery,
        @PositiveOrZero(message = "Номер страницы не может быть отрицательным")
        @Parameter(description = "Номер страницы", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Min(value = 1, message = "Размер страницы должен быть не меньше 1")
        @Max(value = 100, message = "Размер страницы не должен превышать 100")
        @Parameter(description = "Размер страницы", example = "20")
        @RequestParam(defaultValue = "20") int size,
        Pageable pageable
    ) {
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by("id").ascending();
        Pageable effectivePageable = PageRequest.of(page, size, sort);
        return dishService.searchWithFilters(categoryName, ingredientName, namePart, minPrice, maxPrice,
            effectivePageable, useNativeQuery);
    }

    @GetMapping("/dish/{id}")
    @Operation(summary = "Получить блюдо по ID")
    public DishDto getIdByDish(@PathVariable @Positive(message = "ID должен быть больше 0") Long id) {
        return dishService.findById(id);
    }

    @PutMapping("/dish/{id}")
    @Operation(summary = "Обновить блюдо")
    public DishDto updateDish(@PathVariable @Positive(message = "ID должен быть больше 0") Long id,
                              @Valid @RequestBody DishDto dishDto) {
        return dishService.update(id, dishDto);
    }

    @DeleteMapping("/dish/{id}")
    @Operation(summary = "Удалить блюдо")
    public void deleteDish(@PathVariable @Positive(message = "ID должен быть больше 0") Long id) {
        dishService.deleteById(id);
    }

    @GetMapping("/dish/cache/status")
    @Operation(summary = "Получить статус кэша поиска блюд")
    public Map<String, Object> getDishCacheStatus() {
        return dishService.getCacheStatus();
    }
}
