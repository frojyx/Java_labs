package com.example.demo.mapper;

import com.example.demo.dto.OrderDto;
import com.example.demo.entity.Dish;
import com.example.demo.entity.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {
    public OrderDto toDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());

        // Маппинг данных клиента
        if (order.getClient() != null) {
            dto.setClientFirstName(order.getClient().getFirstName());
            dto.setClientLastName(order.getClient().getLastName());
        }

        // Маппинг списка блюд (собираем только названия)
        if (order.getDishes() != null) {
            List<String> dishNames = order.getDishes().stream()
                .map(Dish::getName)
                .collect(Collectors.toList());
            dto.setDishNames(dishNames);
        }

        return dto;
    }

    public List<OrderDto> toDtoList(List<Order> orders) {
        List<OrderDto> list = new ArrayList<>();
        for (Order order : orders) {
            list.add(toDto(order));
        }
        return list;
    }
}
