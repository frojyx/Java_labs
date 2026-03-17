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
    private final DishRepository dishRepository;

    private final DishMapper dishMapper;

    private final CategoryRepository categoryRepository; // 1. Добавь поле

    private final IngredientRepository ingredientRepository;

    private final ClientRepository clientRepository;

    private final OrderRepository orderRepository;

    private final Map<DishSearchCacheKey, Page<DishDto>> searchCache = new ConcurrentHashMap<>();

    // 2. Добавь в конструктор
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

    // Теперь возвращаем DTO, используя метод JpaRepository
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

    // Добавь метод для получения всех блюд (важно для проверки N+1)
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

        return searchCache.computeIfAbsent(key, ignoredKey -> {
            Page<Dish> dishes = useNativeQuery
                ? dishRepository.searchWithFiltersNative(categoryName, ingredientName, namePart, minPrice, maxPrice,
                pageable)
                : dishRepository.searchWithFiltersJpql(categoryName, ingredientName, namePart, minPrice, maxPrice,
                pageable);
            return dishes.map(dishMapper::toDto);
        });
    }


    // Метод сохранения (и для POST, и для PUT)
    @Transactional
    public DishDto save(DishDto dishDto) {
        Dish dish = new Dish();
        dish.setName(dishDto.getName());
        dish.setPrice(dishDto.getPrice());
        dish.setWeight(dishDto.getWeight()); // Сохраняем вес

        // Ищем категорию по имени
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

    // Метод удаления
    @Transactional
    public void deleteById(Long id) {
        Dish dish = dishRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Блюдо с ID " + id + " не найдено"));


        for (Order order : new ArrayList<>(orderRepository.findAll())) {
            List<Dish> dishes = order.getDishes();
            if (dishes != null && dishes.stream().anyMatch(orderDish -> orderDish.getId().equals(id))) {
                Client client = order.getClient();
                orderRepository.delete(order);
                if (client != null && client.getOrders() != null) {
                    client.getOrders().removeIf(existingOrder -> existingOrder.getId().equals(order.getId()));
                    if (client.getOrders().isEmpty()) {
                        clientRepository.delete(client);
                    }
                }
            }
        }

        if (dish.getIngredients() != null) {
            for (Ingredient ingredient : new ArrayList<>(dish.getIngredients())) {
                if (ingredient.getDishes() != null) {
                    ingredient.getDishes().remove(dish);
                }
            }
            dish.getIngredients().clear();
        }

        dish.setCategory(null);
        dishRepository.delete(dish);
        invalidateSearchCache();
    }

    @Transactional
    public DishDto update(Long id, DishDto dishDto) {
        // 1. Ищем существующую сущность по ID
        Dish existingDish = dishRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Блюдо с ID " + id + " не найдено"));

        // 2. Обновляем поля из DTO
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

        // 3. Сохраняем обновленную сущность и возвращаем DTO
        Dish updatedDish = dishRepository.save(existingDish);
        invalidateSearchCache();
        return dishMapper.toDto(updatedDish);
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