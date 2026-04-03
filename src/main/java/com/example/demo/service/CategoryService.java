package com.example.demo.service;

import com.example.demo.dto.CategoryDto;
import com.example.demo.entity.Category;
import com.example.demo.entity.Dish;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.CategoryMapper;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.DishRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
    private static final String CATEGORY_WITH_ID_PREFIX = "Категория с ID ";

    private static final String NOT_FOUND_SUFFIX = " не найдена";

    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    private final DishRepository dishRepository;

    private final DishService dishService;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper,
                           DishRepository dishRepository, DishService dishService) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.dishRepository = dishRepository;
        this.dishService = dishService;
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> findAll() {
        return categoryMapper.toDtoList(categoryRepository.findAll());
    }

    @Transactional(readOnly = true)
    public CategoryDto findById(Long id) {
        return categoryRepository.findById(id)
            .map(categoryMapper::toDto)
            .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX));
    }

    @Transactional
    public CategoryDto save(CategoryDto categoryDto) {
        if (categoryRepository.findByName(categoryDto.getName()).isPresent()) {
            throw new ConflictException("Категория с названием '" + categoryDto.getName() + "' уже существует");
        }

        Category category = categoryMapper.toEntity(categoryDto);
        Category savedCategory = categoryRepository.save(category);
        dishService.invalidateSearchCache();
        return categoryMapper.toDto(savedCategory);
    }

    @Transactional
    public CategoryDto update(Long id, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX));

        categoryRepository.findByName(categoryDto.getName())
            .filter(existingCategory -> !existingCategory.getId().equals(id))
            .ifPresent(existingCategory -> {
                throw new ConflictException("Категория с названием '" + categoryDto.getName() + "' уже существует");
            });

        category.setName(categoryDto.getName());
        Category updatedCategory = categoryRepository.save(category);
        dishService.invalidateSearchCache();
        return categoryMapper.toDto(updatedCategory);
    }

    @Transactional
    public void deleteById(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX));

        List<Dish> dishes = category.getDishes();
        if (dishes != null) {
            for (Dish dish : dishes) {
                dish.setCategory(null);
                dishRepository.save(dish);
            }
        }

        categoryRepository.delete(category);
        dishService.invalidateSearchCache();
    }
}
