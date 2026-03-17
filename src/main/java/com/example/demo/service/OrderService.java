package com.example.demo.service;

import com.example.demo.dto.OrderDto;
import com.example.demo.entity.Client;
import com.example.demo.entity.Dish;
import com.example.demo.entity.Order;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.DishRepository;
import com.example.demo.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    private final ClientRepository clientRepository;

    private final OrderMapper orderMapper;

    private final DishRepository dishRepository;


    public OrderService(OrderRepository orderRepository, ClientRepository clientRepository, OrderMapper orderMapper,
                        DishRepository dishRepository) {
        this.orderRepository = orderRepository;
        this.clientRepository = clientRepository;
        this.orderMapper = orderMapper;
        this.dishRepository = dishRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderDto> findAll() {
        return orderMapper.toDtoList(orderRepository.findAll());
    }

    @Transactional(readOnly = true)
    public OrderDto findById(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Заказ с ID " + id + " не найден"));
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto createNewOrder(OrderDto orderDto) {
        // 1. Создаем клиента
        Client client = new Client();
        client.setFirstName(orderDto.getClientFirstName());
        client.setLastName(orderDto.getClientLastName());
        clientRepository.save(client);

        // 2. Находим блюда по именам
        List<Dish> dishes = dishRepository.findByNameIn(orderDto.getDishNames());

        // 3. Создаем и сохраняем заказ
        Order order = new Order();
        order.setClient(client);
        order.setDishes(dishes); // <--- ВАЖНО: привязываем список блюд
        Order savedOrder = orderRepository.save(order);

        // 4. Возвращаем DTO
        return orderMapper.toDto(savedOrder);
    }

    public void createOrderWithoutTransactionDemo(OrderDto orderDto) {
        createOrderWithFailure(orderDto);
    }

    @Transactional
    public void createOrderWithTransactionDemo(OrderDto orderDto) {
        createOrderWithFailure(orderDto);
    }

    @Transactional
    public OrderDto update(Long id, OrderDto orderDto) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Заказ не найден"));

        // Обновляем данные клиента
        if (order.getClient() != null) {
            order.getClient().setFirstName(orderDto.getClientFirstName());
            order.getClient().setLastName(orderDto.getClientLastName());
        }

        return orderMapper.toDto(orderRepository.save(order));
    }

    @Transactional
    public void deleteById(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Заказ с ID " + id + " не найден"));

        Client client = order.getClient();
        orderRepository.delete(order);

        if (client != null && client.getOrders() != null) {
            client.getOrders().removeIf(existingOrder -> existingOrder.getId().equals(id));
            if (client.getOrders().isEmpty()) {
                clientRepository.delete(client);
            }
        }
    }

    private void createOrderWithFailure(OrderDto orderDto) {
        Client client = new Client();
        client.setFirstName(orderDto.getClientFirstName());
        client.setLastName(orderDto.getClientLastName());
        Client savedClient = clientRepository.save(client);

        List<Dish> dishes = dishRepository.findByNameIn(orderDto.getDishNames());
        if (dishes.size() != orderDto.getDishNames().size()) {
            throw new RuntimeException("Не все блюда найдены. Операция прервана после сохранения клиента.");
        }

        Order order = new Order();
        order.setClient(savedClient);
        order.setDishes(dishes);
        orderRepository.save(order);

        throw new RuntimeException("Искусственная ошибка после сохранения связанных сущностей.");
    }
}
