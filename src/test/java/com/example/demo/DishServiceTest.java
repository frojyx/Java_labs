package com.example.demo;

import com.example.demo.dto.DishDto;
import com.example.demo.entity.Category;
import com.example.demo.entity.Client;
import com.example.demo.entity.Dish;
import com.example.demo.entity.Ingredient;
import com.example.demo.entity.Order;
import com.example.demo.mapper.DishMapper;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.DishRepository;
import com.example.demo.repository.DishSearchNativeProjection;
import com.example.demo.repository.IngredientRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.service.DishService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DishServiceTest {
    @Mock
    private DishRepository dishRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private OrderRepository orderRepository;

    private DishService dishService;

    @BeforeEach
    void initService() {
        dishService = new DishService(
            dishRepository,
            new DishMapper(),
            categoryRepository,
            ingredientRepository,
            clientRepository,
            orderRepository
        );
    }

    @Test
    void findByIdReturnsDto() {
        Dish dish = new Dish();
        dish.setId(1L);
        dish.setName("Soup");
        when(dishRepository.findById(1L)).thenReturn(Optional.of(dish));

        DishDto result = dishService.findById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(dishRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> dishService.findById(1L));
    }

    @Test
    void findByPriceMapsList() {
        Dish first = new Dish();
        first.setName("A");
        Dish second = new Dish();
        second.setName("B");
        when(dishRepository.findByPrice(10.0)).thenReturn(List.of(first, second));

        List<DishDto> result = dishService.findByPrice(10.0);

        assertEquals(2, result.size());
    }

    @Test
    void findAllMapsList() {
        Dish dish = new Dish();
        dish.setName("Soup");
        when(dishRepository.findAll()).thenReturn(List.of(dish));

        List<DishDto> result = dishService.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void searchWithFiltersUsesJpqlAndCacheHit() {
        Pageable pageable = PageRequest.of(0, 5);
        Dish dish = new Dish();
        dish.setName("Soup");
        Page<Dish> page = new PageImpl<>(List.of(dish), pageable, 1);
        when(dishRepository.searchWithFiltersJpql("pasta", "cheese", "carb", 10.0, 20.0,
            PageRequest.of(0, 5, org.springframework.data.domain.Sort.by("id").ascending())))
            .thenReturn(page);

        Page<DishDto> firstCall = dishService.searchWithFilters(" pasta ", " cheese ", " carb ", 10.0, 20.0,
            pageable, false);
        Page<DishDto> secondCall = dishService.searchWithFilters("pasta", "cheese", "carb", 10.0, 20.0,
            pageable, false);

        assertEquals(1, firstCall.getTotalElements());
        assertEquals(1, secondCall.getTotalElements());
        verify(dishRepository, times(1)).searchWithFiltersJpql(any(), any(), any(), any(), any(), any());
    }

    @Test
    void searchWithFiltersUsesNativeAndMapsCsv() {
        Pageable pageable = PageRequest.of(0, 5, org.springframework.data.domain.Sort.by("name"));
        DishSearchNativeProjection projection = new DishSearchNativeProjection() {
            @Override
            public Long getId() {
                return 9L;
            }

            @Override
            public String getName() {
                return "Carbonara";
            }

            @Override
            public double getPrice() {
                return 17.5;
            }

            @Override
            public int getWeight() {
                return 300;
            }

            @Override
            public String getCategory() {
                return "Pasta";
            }

            @Override
            public String getIngredientsCsv() {
                return "Bacon, Cheese";
            }
        };

        when(dishRepository.searchWithFiltersNative("pasta", null, null, null, null, pageable))
            .thenReturn(new PageImpl<>(List.of(projection), pageable, 1));

        Page<DishDto> result = dishService.searchWithFilters("Pasta", " ", " ", null, null, pageable, true);

        assertEquals(1, result.getTotalElements());
        assertEquals(2, result.getContent().get(0).getIngredients().size());
    }

    @Test
    void saveStoresDishWithoutCategoryWhenNull() {
        DishDto dto = buildDishDto("Soup", null, 8.0, 250, List.of());
        when(dishRepository.save(any(Dish.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DishDto result = dishService.save(dto);

        assertEquals("Soup", result.getName());
        assertNull(result.getCategory());
    }

    @Test
    void saveStoresDishWithCategoryAndIngredients() {
        DishDto dto = buildDishDto("Soup", "Hot", 8.0, 250, List.of("Salt"));
        Category category = new Category();
        category.setName("Hot");
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Salt");
        when(categoryRepository.findByName("Hot")).thenReturn(Optional.of(category));
        when(ingredientRepository.findByNameIn(List.of("Salt"))).thenReturn(List.of(ingredient));
        when(dishRepository.save(any(Dish.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DishDto result = dishService.save(dto);

        assertEquals("Hot", result.getCategory());
    }

    @Test
    void saveThrowsWhenCategoryNotFound() {
        DishDto dto = buildDishDto("Soup", "Missing", 8.0, 250, List.of());
        when(categoryRepository.findByName("Missing")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> dishService.save(dto));
    }

    @Test
    void saveThrowsWhenIngredientMissing() {
        DishDto dto = buildDishDto("Soup", null, 8.0, 250, List.of("Salt", "Pepper"));
        when(ingredientRepository.findByNameIn(List.of("Salt", "Pepper")))
            .thenReturn(List.of(new Ingredient()));

        assertThrows(RuntimeException.class, () -> dishService.save(dto));
    }

    @Test
    void updateThrowsWhenDishMissing() {
        when(dishRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> dishService.update(1L, new DishDto()));
    }

    @Test
    void updateThrowsWhenCategoryNotFound() {
        Dish existing = new Dish();
        when(dishRepository.findById(1L)).thenReturn(Optional.of(existing));
        DishDto dto = buildDishDto("Soup", "Missing", 8.0, 250, List.of());
        when(categoryRepository.findByName("Missing")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> dishService.update(1L, dto));
    }

    @Test
    void updateResetsCategoryWhenNull() {
        Dish existing = new Dish();
        Category old = new Category();
        existing.setCategory(old);
        when(dishRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(dishRepository.save(existing)).thenReturn(existing);
        DishDto dto = buildDishDto("Soup", null, 8.0, 250, List.of());

        dishService.update(1L, dto);

        assertNull(existing.getCategory());
    }

    @Test
    void updateSetsCategoryWhenProvided() {
        Dish existing = new Dish();
        DishDto dto = buildDishDto("Soup", "Hot", 8.0, 250, List.of("Salt"));
        Category category = new Category();
        category.setName("Hot");
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Salt");

        when(dishRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByName("Hot")).thenReturn(Optional.of(category));
        when(ingredientRepository.findByNameIn(List.of("Salt"))).thenReturn(List.of(ingredient));
        when(dishRepository.save(existing)).thenReturn(existing);

        dishService.update(1L, dto);

        assertEquals("Hot", existing.getCategory().getName());
    }

    @Test
    void deleteByIdThrowsWhenMissing() {
        when(dishRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> dishService.deleteById(1L));
    }

    @Test
    void deleteByIdHandlesOrdersIngredientsAndClientCleanup() {
        Dish dishToDelete = new Dish();
        dishToDelete.setId(5L);

        Ingredient ingredientWithNullDishList = new Ingredient();
        ingredientWithNullDishList.setDishes(null);
        Ingredient ingredientWithDishList = new Ingredient();
        ingredientWithDishList.setDishes(new ArrayList<>(List.of(dishToDelete)));
        dishToDelete.setIngredients(new ArrayList<>(List.of(ingredientWithNullDishList, ingredientWithDishList)));

        Order orderWithTargetDish = new Order();
        orderWithTargetDish.setId(10L);
        orderWithTargetDish.setDishes(List.of(dishToDelete));
        Client client = new Client();
        client.setOrders(new ArrayList<>(List.of(orderWithTargetDish)));
        orderWithTargetDish.setClient(client);

        Order orderWithoutTargetDish = new Order();
        orderWithoutTargetDish.setId(11L);
        Dish anotherDish = new Dish();
        anotherDish.setId(99L);
        orderWithoutTargetDish.setDishes(List.of(anotherDish));

        when(dishRepository.findById(5L)).thenReturn(Optional.of(dishToDelete));
        when(orderRepository.findAll()).thenReturn(List.of(orderWithTargetDish, orderWithoutTargetDish));

        dishService.deleteById(5L);

        verify(orderRepository).delete(orderWithTargetDish);
        verify(orderRepository, never()).delete(orderWithoutTargetDish);
        verify(clientRepository).delete(client);
        verify(dishRepository).delete(dishToDelete);
        assertNull(dishToDelete.getCategory());
        assertEquals(0, dishToDelete.getIngredients().size());
    }

    @Test
    void deleteByIdSkipsClientDeletionWhenClientNullOrOrdersNullOrNotEmpty() {
        Dish dishToDelete = new Dish();
        dishToDelete.setId(5L);
        dishToDelete.setIngredients(null);

        Order nullClientOrder = new Order();
        nullClientOrder.setId(1L);
        nullClientOrder.setClient(null);
        nullClientOrder.setDishes(List.of(dishToDelete));

        Order nullOrdersClientOrder = new Order();
        nullOrdersClientOrder.setId(2L);
        Client clientWithNullOrders = new Client();
        clientWithNullOrders.setOrders(null);
        nullOrdersClientOrder.setClient(clientWithNullOrders);
        nullOrdersClientOrder.setDishes(List.of(dishToDelete));

        Order nonEmptyOrdersClientOrder = new Order();
        nonEmptyOrdersClientOrder.setId(3L);
        Order another = new Order();
        another.setId(4L);
        Client clientWithManyOrders = new Client();
        clientWithManyOrders.setOrders(new ArrayList<>(List.of(nonEmptyOrdersClientOrder, another)));
        nonEmptyOrdersClientOrder.setClient(clientWithManyOrders);
        nonEmptyOrdersClientOrder.setDishes(List.of(dishToDelete));

        when(dishRepository.findById(5L)).thenReturn(Optional.of(dishToDelete));
        when(orderRepository.findAll()).thenReturn(
            List.of(nullClientOrder, nullOrdersClientOrder, nonEmptyOrdersClientOrder)
        );

        dishService.deleteById(5L);

        verify(clientRepository, never()).delete(any(Client.class));
    }

    @Test
    void deleteByIdSkipsOrderWithoutDishesList() {
        Dish dishToDelete = new Dish();
        dishToDelete.setId(5L);
        dishToDelete.setIngredients(null);

        Order order = new Order();
        order.setId(1L);
        order.setDishes(null);

        when(dishRepository.findById(5L)).thenReturn(Optional.of(dishToDelete));
        when(orderRepository.findAll()).thenReturn(List.of(order));

        dishService.deleteById(5L);

        verify(orderRepository, never()).delete(order);
    }

    @Test
    void invalidateSearchCacheAndGetStatusWork() {
        Pageable pageable = PageRequest.of(0, 1, org.springframework.data.domain.Sort.by("id"));
        Dish dish = new Dish();
        dish.setName("Soup");
        when(dishRepository.searchWithFiltersJpql(null, null, null, null, null, pageable))
            .thenReturn(new PageImpl<>(List.of(dish), pageable, 1));

        dishService.searchWithFilters(null, null, null, null, null, pageable, false);
        Map<String, Object> cacheBefore = dishService.getCacheStatus();
        dishService.invalidateSearchCache();
        Map<String, Object> cacheAfter = dishService.getCacheStatus();

        assertEquals(1, cacheBefore.get("size"));
        assertEquals(0, cacheAfter.get("size"));
        assertNotNull(cacheBefore.get("keys"));
    }

    @Test
    void searchWithFiltersNativeMapsBlankCsvToEmptyList() {
        Pageable pageable = PageRequest.of(0, 1, org.springframework.data.domain.Sort.by("id"));
        DishSearchNativeProjection projection = new DishSearchNativeProjection() {
            @Override
            public Long getId() {
                return 1L;
            }

            @Override
            public String getName() {
                return "Dish";
            }

            @Override
            public double getPrice() {
                return 10.0;
            }

            @Override
            public int getWeight() {
                return 200;
            }

            @Override
            public String getCategory() {
                return "Cat";
            }

            @Override
            public String getIngredientsCsv() {
                return " ";
            }
        };
        when(dishRepository.searchWithFiltersNative(null, null, null, null, null, pageable))
            .thenReturn(new PageImpl<>(List.of(projection), pageable, 1));

        Page<DishDto> result = dishService.searchWithFilters(null, null, null, null, null, pageable, true);

        assertEquals(0, result.getContent().get(0).getIngredients().size());
    }

    @Test
    void searchWithFiltersNativeDropsBlankCsvTokens() {
        Pageable pageable = PageRequest.of(0, 1, org.springframework.data.domain.Sort.by("id"));
        DishSearchNativeProjection projection = new DishSearchNativeProjection() {
            @Override
            public Long getId() {
                return 1L;
            }

            @Override
            public String getName() {
                return "Dish";
            }

            @Override
            public double getPrice() {
                return 10.0;
            }

            @Override
            public int getWeight() {
                return 200;
            }

            @Override
            public String getCategory() {
                return "Cat";
            }

            @Override
            public String getIngredientsCsv() {
                return "Salt, ,Pepper";
            }
        };
        when(dishRepository.searchWithFiltersNative(null, null, null, null, null, pageable))
            .thenReturn(new PageImpl<>(List.of(projection), pageable, 1));

        Page<DishDto> result = dishService.searchWithFilters(null, null, null, null, null, pageable, true);

        assertEquals(List.of("Salt", "Pepper"), result.getContent().get(0).getIngredients());
    }

    @Test
    void searchWithFiltersNativeHandlesNullCsv() {
        Pageable pageable = PageRequest.of(0, 1, org.springframework.data.domain.Sort.by("id"));
        DishSearchNativeProjection projection = new DishSearchNativeProjection() {
            @Override
            public Long getId() {
                return 1L;
            }

            @Override
            public String getName() {
                return "Dish";
            }

            @Override
            public double getPrice() {
                return 10.0;
            }

            @Override
            public int getWeight() {
                return 200;
            }

            @Override
            public String getCategory() {
                return "Cat";
            }

            @Override
            public String getIngredientsCsv() {
                return null;
            }
        };
        when(dishRepository.searchWithFiltersNative(null, null, null, null, null, pageable))
            .thenReturn(new PageImpl<>(List.of(projection), pageable, 1));

        Page<DishDto> result = dishService.searchWithFilters(null, null, null, null, null, pageable, true);

        assertEquals(0, result.getContent().get(0).getIngredients().size());
    }

    @Test
    void saveHandlesNullIngredientList() {
        DishDto dto = buildDishDto("Soup", null, 8.0, 250, null);
        when(dishRepository.save(any(Dish.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DishDto result = dishService.save(dto);

        assertEquals("Soup", result.getName());
    }

    private DishDto buildDishDto(String name, String category, double price, int weight, List<String> ingredients) {
        DishDto dto = new DishDto();
        dto.setName(name);
        dto.setCategory(category);
        dto.setPrice(price);
        dto.setWeight(weight);
        dto.setIngredients(ingredients);
        return dto;
    }
}
