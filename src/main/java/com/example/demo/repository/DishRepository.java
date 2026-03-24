package com.example.demo.repository;

import com.example.demo.entity.Dish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
    @NonNull
    @EntityGraph(attributePaths = {"category", "ingredients"})
    List<Dish> findAll();

    List<Dish> findByNameIn(List<String> names);

    List<Dish> findByPrice(double price);

    @EntityGraph(attributePaths = {"category", "ingredients"})
    @Query("""
        select distinct d
        from Dish d
        left join d.category c
        left join d.ingredients i
        where (:categoryName is null or lower(c.name) = lower(:categoryName))
          and (:ingredientName is null or lower(i.name) = lower(:ingredientName))
          and (:namePart is null or lower(d.name) like lower(concat('%', :namePart, '%')))
          and (:minPrice is null or d.price >= :minPrice)
          and (:maxPrice is null or d.price <= :maxPrice)
        """)
    Page<Dish> searchWithFiltersJpql(
        @Param("categoryName") String categoryName,
        @Param("ingredientName") String ingredientName,
        @Param("namePart") String namePart,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        Pageable pageable
    );

    @Query(value = """
        select distinct d.*
        from dishes d
        left join categories c on c.id = d.category_id
        left join dish_ingredients di on di.dish_id = d.id
        left join ingredients i on i.id = di.ingredient_id
        where (:categoryName is null or lower(c.name) = lower(:categoryName))
          and (:ingredientName is null or lower(i.name) = lower(:ingredientName))
          and (:namePart is null or lower(d.name) like lower(concat('%', :namePart, '%')))
          and (:minPrice is null or d.price >= :minPrice)
          and (:maxPrice is null or d.price <= :maxPrice)
        """,
        nativeQuery = true)
    Page<Dish> searchWithFiltersNative(
        @Param("categoryName") String categoryName,
        @Param("ingredientName") String ingredientName,
        @Param("namePart") String namePart,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        Pageable pageable
    );
}
