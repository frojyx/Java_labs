package com.example.demo.controller;

import com.example.demo.dto.DishDto;
import com.example.demo.service.DishService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DishController {

    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @GetMapping("/dish/{category}")
    public List<DishDto> getCategoryByDish(@PathVariable String category) {
        return dishService.findByCategory(category);
    }


    @GetMapping("/dish")
    public List<DishDto> getDishByPrice(@RequestParam double price) {
        return dishService.findByPrice(price);
    }
}
