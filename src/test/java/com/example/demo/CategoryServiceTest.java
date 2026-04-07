package com.example.demo;

import com.example.demo.dto.CategoryDto;
import com.example.demo.entity.Category;
import com.example.demo.entity.Dish;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.CategoryMapper;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.DishRepository;
import com.example.demo.service.CategoryService;
import com.example.demo.service.DishService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private DishRepository dishRepository;

    private TrackingDishService dishService;

    private CategoryService categoryService;

    @BeforeEach
    void initService() {
        dishService = new TrackingDishService();
        categoryService = new CategoryService(categoryRepository, new CategoryMapper(), dishRepository, dishService);
    }

    @Test
    void findAllReturnsMappedList() {
        when(categoryRepository.findAll()).thenReturn(List.of(new Category()));

        List<CategoryDto> result = categoryService.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void findByIdReturnsDto() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Soups");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryDto result = categoryService.findById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Soups", result.getName());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> categoryService.findById(1L));
    }

    @Test
    void saveThrowsWhenNameExists() {
        CategoryDto dto = new CategoryDto();
        dto.setName("Pasta");
        when(categoryRepository.findByName("Pasta")).thenReturn(Optional.of(new Category()));

        assertThrows(ConflictException.class, () -> categoryService.save(dto));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void saveStoresCategoryAndInvalidatesCache() {
        CategoryDto dto = new CategoryDto();
        dto.setName("Pasta");
        when(categoryRepository.findByName("Pasta")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryDto result = categoryService.save(dto);

        assertEquals("Pasta", result.getName());
        assertEquals(1, dishService.invalidateCalls);
    }

    @Test
    void updateThrowsWhenCategoryMissing() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> categoryService.update(1L, new CategoryDto()));
    }

    @Test
    void updateThrowsWhenNameBelongsToAnotherCategory() {
        Category existing = new Category();
        existing.setId(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));

        Category conflict = new Category();
        conflict.setId(2L);
        when(categoryRepository.findByName("Soup")).thenReturn(Optional.of(conflict));

        CategoryDto dto = new CategoryDto();
        dto.setName("Soup");

        assertThrows(ConflictException.class, () -> categoryService.update(1L, dto));
    }

    @Test
    void updateAllowsSameNameForSameCategoryId() {
        Category existing = new Category();
        existing.setId(1L);
        existing.setName("Old");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByName("Soup")).thenReturn(Optional.of(existing));
        when(categoryRepository.save(existing)).thenReturn(existing);

        CategoryDto dto = new CategoryDto();
        dto.setName("Soup");

        CategoryDto result = categoryService.update(1L, dto);

        assertEquals("Soup", result.getName());
        assertEquals(1, dishService.invalidateCalls);
    }

    @Test
    void deleteByIdThrowsWhenMissing() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteById(1L));
    }

    @Test
    void deleteByIdHandlesNullDishesList() {
        Category category = new Category();
        category.setDishes(null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.deleteById(1L);

        verify(categoryRepository).delete(category);
        assertEquals(1, dishService.invalidateCalls);
        verify(dishRepository, never()).save(any(Dish.class));
    }

    @Test
    void deleteByIdDetachesAllDishes() {
        Dish first = new Dish();
        first.setCategory(new Category());
        Dish second = new Dish();
        second.setCategory(new Category());
        Category category = new Category();
        category.setDishes(new ArrayList<>(List.of(first, second)));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.deleteById(1L);

        verify(dishRepository).save(first);
        verify(dishRepository).save(second);
        verify(categoryRepository).delete(category);
        assertEquals(1, dishService.invalidateCalls);
    }

    private static class TrackingDishService extends DishService {
        private int invalidateCalls;

        TrackingDishService() {
            super(null, null, null, null, null, null);
        }

        @Override
        public void invalidateSearchCache() {
            invalidateCalls++;
        }
    }
}
