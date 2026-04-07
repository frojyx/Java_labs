package com.example.demo;

import com.example.demo.dto.IngredientDto;
import com.example.demo.entity.Dish;
import com.example.demo.entity.Ingredient;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.IngredientMapper;
import com.example.demo.repository.IngredientRepository;
import com.example.demo.service.DishService;
import com.example.demo.service.IngredientService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {
    @Mock
    private IngredientRepository ingredientRepository;

    private TrackingDishService dishService;

    private IngredientService ingredientService;

    @BeforeEach
    void initService() {
        dishService = new TrackingDishService();
        ingredientService = new IngredientService(ingredientRepository, new IngredientMapper(), dishService);
    }

    @Test
    void findAllReturnsMappedList() {
        when(ingredientRepository.findAll()).thenReturn(List.of(new Ingredient()));

        List<IngredientDto> result = ingredientService.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void findByIdReturnsDto() {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(1L);
        ingredient.setName("Cheese");
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));

        IngredientDto result = ingredientService.findById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(ingredientRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> ingredientService.findById(1L));
    }

    @Test
    void saveThrowsWhenNameExists() {
        IngredientDto dto = new IngredientDto();
        dto.setName("Cheese");
        when(ingredientRepository.findByName("Cheese")).thenReturn(Optional.of(new Ingredient()));

        assertThrows(ConflictException.class, () -> ingredientService.save(dto));
    }

    @Test
    void saveStoresIngredientAndInvalidatesCache() {
        IngredientDto dto = new IngredientDto();
        dto.setName("Cheese");
        when(ingredientRepository.findByName("Cheese")).thenReturn(Optional.empty());
        when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IngredientDto result = ingredientService.save(dto);

        assertEquals("Cheese", result.getName());
        assertEquals(1, dishService.invalidateCalls);
    }

    @Test
    void updateThrowsWhenMissing() {
        when(ingredientRepository.findById(1L)).thenReturn(Optional.empty());
        IngredientDto ingredientDto = new IngredientDto();
        assertThrows(ResourceNotFoundException.class, () -> ingredientService.update(1L, ingredientDto));
    }

    @Test
    void updateThrowsWhenNameBelongsToAnotherIngredient() {
        Ingredient existing = new Ingredient();
        existing.setId(1L);
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(existing));

        Ingredient conflict = new Ingredient();
        conflict.setId(2L);
        when(ingredientRepository.findByName("Cheese")).thenReturn(Optional.of(conflict));

        IngredientDto dto = new IngredientDto();
        dto.setName("Cheese");

        assertThrows(ConflictException.class, () -> ingredientService.update(1L, dto));
    }

    @Test
    void updateAllowsSameNameForSameId() {
        Ingredient existing = new Ingredient();
        existing.setId(1L);
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(ingredientRepository.findByName("Cheese")).thenReturn(Optional.of(existing));
        when(ingredientRepository.save(existing)).thenReturn(existing);

        IngredientDto dto = new IngredientDto();
        dto.setName("Cheese");

        IngredientDto result = ingredientService.update(1L, dto);

        assertEquals("Cheese", result.getName());
        assertEquals(1, dishService.invalidateCalls);
    }

    @Test
    void deleteByIdThrowsWhenMissing() {
        when(ingredientRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> ingredientService.deleteById(1L));
    }

    @Test
    void deleteByIdHandlesNullDishList() {
        Ingredient ingredient = new Ingredient();
        ingredient.setDishes(null);
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));

        ingredientService.deleteById(1L);

        assertEquals(0, dishService.deleteIds.size());
        verify(ingredientRepository).delete(ingredient);
        assertEquals(1, dishService.invalidateCalls);
    }

    @Test
    void deleteByIdDeletesRelatedDishes() {
        Dish first = new Dish();
        first.setId(1L);
        Dish second = new Dish();
        second.setId(2L);
        Ingredient ingredient = new Ingredient();
        ingredient.setDishes(new ArrayList<>(List.of(first, second)));
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));

        ingredientService.deleteById(1L);

        assertEquals(List.of(1L, 2L), dishService.deleteIds);
        verify(ingredientRepository).delete(ingredient);
        assertEquals(1, dishService.invalidateCalls);
    }

    private static class TrackingDishService extends DishService {
        private final List<Long> deleteIds = new ArrayList<>();
        private int invalidateCalls;

        TrackingDishService() {
            super(null, null, null, null, null, null);
        }

        @Override
        public void deleteById(Long id) {
            deleteIds.add(id);
        }

        @Override
        public void invalidateSearchCache() {
            invalidateCalls++;
        }
    }
}
