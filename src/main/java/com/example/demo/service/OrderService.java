package com.example.demo.service;

import com.example.demo.dto.OrderDto;
import com.example.demo.entity.Client;
import com.example.demo.entity.Dish;
import com.example.demo.entity.Order;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UnprocessableEntityException;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.DishRepository;
import com.example.demo.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private static final String ORDER_WITH_ID_PREFIX = "Order with ID ";
    private static final String ORDER_NOT_FOUND_SUFFIX = " not found";

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
            .orElseThrow(() -> new ResourceNotFoundException(ORDER_WITH_ID_PREFIX + id + ORDER_NOT_FOUND_SUFFIX));
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto createNewOrder(OrderDto orderDto) {
        List<String> normalizedDishNames = validateAndNormalizeDishNames(orderDto.getDishNames());

        Client client = new Client();
        client.setFirstName(orderDto.getClientFirstName());
        client.setLastName(orderDto.getClientLastName());
        clientRepository.save(client);

        List<Dish> dishes = resolveDishesOrThrow(normalizedDishNames);

        Order order = new Order();
        order.setClient(client);
        order.setDishes(dishes);
        Order savedOrder = orderRepository.save(order);

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
            .orElseThrow(() -> new ResourceNotFoundException(ORDER_WITH_ID_PREFIX + id + ORDER_NOT_FOUND_SUFFIX));

        if (order.getClient() != null) {
            order.getClient().setFirstName(orderDto.getClientFirstName());
            order.getClient().setLastName(orderDto.getClientLastName());
        }

        return orderMapper.toDto(orderRepository.save(order));
    }

    @Transactional
    public void deleteById(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ORDER_WITH_ID_PREFIX + id + ORDER_NOT_FOUND_SUFFIX));

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
        List<String> normalizedDishNames = validateAndNormalizeDishNames(orderDto.getDishNames());

        Client client = new Client();
        client.setFirstName(orderDto.getClientFirstName());
        client.setLastName(orderDto.getClientLastName());
        Client savedClient = clientRepository.save(client);

        List<Dish> dishes = resolveDishesOrThrow(normalizedDishNames);

        Order order = new Order();
        order.setClient(savedClient);
        order.setDishes(dishes);
        orderRepository.save(order);

        throw new RuntimeException("Artificial failure after saving related entities.");
    }

    private List<String> validateAndNormalizeDishNames(List<String> dishNames) {
        if (dishNames == null) {
            throw new BadRequestException("Dish list is required");
        }

        List<String> normalizedDishNames = dishNames.stream()
            .map(name -> name == null ? "" : name.trim())
            .toList();

        if (normalizedDishNames.stream().anyMatch(String::isBlank)) {
            throw new BadRequestException("Dish names must not be blank");
        }

        Set<String> uniqueDishNames = new LinkedHashSet<>(normalizedDishNames);
        if (uniqueDishNames.size() != normalizedDishNames.size()) {
            throw new BadRequestException("Dish list contains duplicates");
        }

        return normalizedDishNames;
    }

    private List<Dish> resolveDishesOrThrow(List<String> dishNames) {
        List<Dish> dishes = dishRepository.findByNameIn(dishNames);
        if (dishes.size() == dishNames.size()) {
            return dishes;
        }

        Set<String> foundDishNames = dishes.stream()
            .map(Dish::getName)
            .collect(Collectors.toSet());
        List<String> missingDishNames = dishNames.stream()
            .filter(name -> !foundDishNames.contains(name))
            .distinct()
            .toList();

        throw new UnprocessableEntityException(
            "Cannot create order. Dishes not found: " + String.join(", ", missingDishNames)
        );
    }
}
