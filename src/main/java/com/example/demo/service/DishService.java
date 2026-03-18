package com.example.demo.service;

import com.example.demo.cache.DishSearchCacheKey;
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
import com.example.demo.repository.IngredientRepository;
import com.example.demo.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class DishService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DishService.class);

    private final DishRepository dishRepository;

    private final DishMapper dishMapper;

    private final CategoryRepository categoryRepository;

    private final IngredientRepository ingredientRepository;

    private final ClientRepository clientRepository;

    private final OrderRepository orderRepository;

    private final Map<DishSearchCacheKey, Page<DishDto>> searchCache = new ConcurrentHashMap<>();

    public DishService(DishRepository dishRepository, DishMapper dishMapper, CategoryRepository categoryRepository,
                       IngredientRepository ingredientRepository, ClientRepository clientRepository,
                       OrderRepository orderRepository) {
        this.dishRepository = dishRepository;
        this.dishMapper = dishMapper;
        this.categoryRepository = categoryRepository;
        this.ingredientRepository = ingredientRepository;
        this.clientRepository = clientRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public DishDto findById(Long id) {
        return dishRepository.findById(id)
            .map(dishMapper::toDto)
            .orElseThrow(() -> new RuntimeException("Блюдо не найдено"));
    }

    @Transactional(readOnly = true)
    public List<DishDto> findByPrice(double price) {
        List<Dish> dishes = dishRepository.findByPrice(price);
        return dishes.stream()
            .map(dishMapper::toDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DishDto> findAll() {
        return dishMapper.toDtoList(dishRepository.findAll());
    }

    @Transactional(readOnly = true)
    public Page<DishDto> searchWithFilters(String categoryName, String ingredientName, String namePart,
                                           Double minPrice, Double maxPrice, Pageable pageable,
                                           boolean useNativeQuery) {
        DishSearchCacheKey key = new DishSearchCacheKey(useNativeQuery, categoryName, ingredientName, namePart,
            minPrice, maxPrice, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        Page<DishDto> cachedResult = searchCache.get(key);
        if (cachedResult != null) {
            LOGGER.debug("Dish search cache HIT: {}", key);
            return cachedResult;
        }

        LOGGER.debug("Dish search cache MISS: {}", key);
        Page<DishDto> computedResult = findWithSelectedQuery(categoryName, ingredientName, namePart, minPrice,
            maxPrice, pageable, useNativeQuery)
            .map(dishMapper::toDto);

        Page<DishDto> existingResult = searchCache.putIfAbsent(key, computedResult);
        if (existingResult != null) {
            LOGGER.debug("Dish search cache filled concurrently, using existing value: {}", key);
            return existingResult;
        }

        return computedResult;
    }

    @Transactional
    public DishDto save(DishDto dishDto) {
        Dish dish = new Dish();
        dish.setName(dishDto.getName());
        dish.setPrice(dishDto.getPrice());
        dish.setWeight(dishDto.getWeight());

        if (dishDto.getCategory() != null) {
            Category category = categoryRepository.findByName(dishDto.getCategory())
                .orElseThrow(() -> new RuntimeException("Категория '" + dishDto.getCategory() + "' не найдена"));
            dish.setCategory(category);
        }

        dish.setIngredients(resolveIngredients(dishDto.getIngredients()));

        Dish savedDish = dishRepository.save(dish);
        invalidateSearchCache();
        return dishMapper.toDto(savedDish);
    }

    @Transactional
    public void deleteById(Long id) {
        Dish dish = dishRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Блюдо с ID " + id + " не найдено"));

        removeDishFromOrdersAndCleanupClients(id);
        detachDishFromIngredients(dish);

        dish.setCategory(null);
        dishRepository.delete(dish);
        invalidateSearchCache();
    }

    @Transactional
    public DishDto update(Long id, DishDto dishDto) {
        Dish existingDish = dishRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Блюдо с ID " + id + " не найдено"));

        existingDish.setName(dishDto.getName());
        existingDish.setPrice(dishDto.getPrice());
        existingDish.setWeight(dishDto.getWeight());
        if (dishDto.getCategory() != null) {
            Category category = categoryRepository.findByName(dishDto.getCategory())
                .orElseThrow(() -> new RuntimeException("Категория '" + dishDto.getCategory() + "' не найдена"));
            existingDish.setCategory(category);
        } else {
            existingDish.setCategory(null);
        }
        existingDish.setIngredients(resolveIngredients(dishDto.getIngredients()));

        Dish updatedDish = dishRepository.save(existingDish);
        invalidateSearchCache();
        return dishMapper.toDto(updatedDish);
    }

    private Page<Dish> findWithSelectedQuery(String categoryName, String ingredientName, String namePart,
                                             Double minPrice, Double maxPrice, Pageable pageable,
                                             boolean useNativeQuery) {
        if (useNativeQuery) {
            return dishRepository.searchWithFiltersNative(categoryName, ingredientName, namePart, minPrice, maxPrice,
                pageable);
        }
        return dishRepository.searchWithFiltersJpql(categoryName, ingredientName, namePart, minPrice, maxPrice,
            pageable);
    }

    private void removeDishFromOrdersAndCleanupClients(Long dishId) {
        for (Order order : new ArrayList<>(orderRepository.findAll())) {
            if (!containsDish(order, dishId)) {
                continue;
            }

            Client client = order.getClient();
            orderRepository.delete(order);
            removeOrderFromClient(client, order.getId());
        }
    }

    private boolean containsDish(Order order, Long dishId) {
        List<Dish> dishes = order.getDishes();
        return dishes != null && dishes.stream().anyMatch(orderDish -> orderDish.getId().equals(dishId));
    }

    private void removeOrderFromClient(Client client, Long orderId) {
        if (client == null || client.getOrders() == null) {
            return;
        }

        client.getOrders().removeIf(existingOrder -> existingOrder.getId().equals(orderId));
        if (client.getOrders().isEmpty()) {
            clientRepository.delete(client);
        }
    }

    private void detachDishFromIngredients(Dish dish) {
        if (dish.getIngredients() == null) {
            return;
        }

        for (Ingredient ingredient : new ArrayList<>(dish.getIngredients())) {
            if (ingredient.getDishes() != null) {
                ingredient.getDishes().remove(dish);
            }
        }
        dish.getIngredients().clear();
    }

    private List<Ingredient> resolveIngredients(List<String> ingredientNames) {
        if (ingredientNames == null || ingredientNames.isEmpty()) {
            return new ArrayList<>();
        }

        List<Ingredient> ingredients = ingredientRepository.findByNameIn(ingredientNames);
        if (ingredients.size() != ingredientNames.size()) {
            throw new RuntimeException("Не все ингредиенты найдены");
        }
        return ingredients;
    }

    private void invalidateSearchCache() {
        searchCache.clear();
    }
}

