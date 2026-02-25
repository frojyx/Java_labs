package com.example.demo.service;

import com.example.demo.dto.DishDto;
import com.example.demo.entity.Dish;
import com.example.demo.mapper.DishMapper;
import com.example.demo.repository.DishRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishService {
    private final DishRepository dishRepository;

    private final DishMapper dishMapper;

    public DishService(DishRepository dishRepository, DishMapper dishMapper) {
        this.dishRepository = dishRepository;
        this.dishMapper = dishMapper;
    }

    public List<DishDto> findByCategory(String category) {
        List<Dish> dishes = dishRepository.findByCategory(category);
        return dishMapper.toDtoList(dishes);
    }

    public List<DishDto> findByPrice(double price) {
        List<Dish> dishes = dishRepository.findByPrice(price);
        return dishMapper.toDtoList(dishes);
    }
}
