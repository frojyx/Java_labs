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
import com.example.demo.repository.DishSearchNativeProjection;
import com.example.demo.repository.IngredientRepository;
import com.example.demo.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final Map<DishSearchCacheKey, Page<DishDto>> searchCache = new HashMap<>();

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
        Pageable effectivePageable = normalizePageable(pageable);
        DishSearchCacheKey key = new DishSearchCacheKey(useNativeQuery, categoryName, ingredientName, namePart,
            minPrice, maxPrice, effectivePageable.getPageNumber(), effectivePageable.getPageSize(),
            effectivePageable.getSort());

        Page<DishDto> cachedResult = searchCache.get(key);
        if (cachedResult != null) {
            LOGGER.debug("Dish search cache HIT: {}", key);
            return cachedResult;
        }

        LOGGER.debug("Dish search cache MISS: {}", key);
        Page<DishDto> computedResult = findWithSelectedQuery(categoryName, ingredientName, namePart, minPrice,
            maxPrice, effectivePageable, useNativeQuery);

        searchCache.put(key, computedResult);
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

    private Page<DishDto> findWithSelectedQuery(String categoryName, String ingredientName, String namePart,
                                                Double minPrice, Double maxPrice, Pageable pageable,
                                                boolean useNativeQuery) {
        if (useNativeQuery) {
            return findWithNativeQuery(categoryName, ingredientName, namePart, minPrice, maxPrice, pageable);
        }
        return dishRepository.searchWithFiltersJpql(categoryName, ingredientName, namePart, minPrice, maxPrice,
            pageable).map(dishMapper::toDto);
    }

    private Page<DishDto> findWithNativeQuery(String categoryName, String ingredientName, String namePart,
                                              Double minPrice, Double maxPrice, Pageable pageable) {
        return dishRepository.searchWithFiltersNative(
            categoryName, ingredientName, namePart, minPrice, maxPrice, pageable
        ).map(this::mapNativeProjectionToDto);
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

    public void invalidateSearchCache() {
        searchCache.clear();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCacheStatus() {
        List<String> keys = searchCache.keySet().stream()
            .map(DishSearchCacheKey::toString)
            .sorted()
            .toList();

        return Map.of(
            "cacheName", "dishSearchCache",
            "size", searchCache.size(),
            "keys", keys
        );
    }

    private DishDto mapNativeProjectionToDto(DishSearchNativeProjection projection) {
        DishDto dishDto = new DishDto();
        dishDto.setId(projection.getId());
        dishDto.setName(projection.getName());
        dishDto.setPrice(projection.getPrice());
        dishDto.setWeight(projection.getWeight());
        dishDto.setCategory(projection.getCategory());
        dishDto.setIngredients(parseIngredientsCsv(projection.getIngredientsCsv()));
        return dishDto;
    }

    private List<String> parseIngredientsCsv(String ingredientsCsv) {
        if (ingredientsCsv == null || ingredientsCsv.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(ingredientsCsv.split(","))
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .toList();
    }

    private Pageable normalizePageable(Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id").ascending());
    }

}
