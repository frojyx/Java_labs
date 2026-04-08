package com.example.demo.controller;

import com.example.demo.dto.OrderDto;
import com.example.demo.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Operations with orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "Get all orders")
    public List<OrderDto> getAllOrders() {
        return orderService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public OrderDto getOrderById(@PathVariable @Positive(message = "ID must be greater than 0") Long id) {
        return orderService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create order")
    public OrderDto createOrder(@Valid @RequestBody OrderDto orderDto) {
        return orderService.createNewOrder(orderDto);
    }

    @PostMapping("/bulk")
    @Operation(summary = "Create orders in bulk")
    public List<OrderDto> createOrdersBulk(@Valid @RequestBody List<@Valid OrderDto> orderDtos) {
        return orderService.createOrdersBulk(orderDtos);
    }

    @PostMapping("/bulk/no-transaction")
    @Operation(summary = "Demo bulk operation without transaction")
    public List<OrderDto> createOrdersBulkWithoutTransaction(@Valid @RequestBody List<@Valid OrderDto> orderDtos) {
        return orderService.createOrdersBulkWithoutTransactionDemo(orderDtos);
    }

    @PostMapping("/bulk/transaction")
    @Operation(summary = "Demo bulk operation with transaction")
    public List<OrderDto> createOrdersBulkWithTransaction(@Valid @RequestBody List<@Valid OrderDto> orderDtos) {
        return orderService.createOrdersBulkWithTransactionDemo(orderDtos);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update order")
    public OrderDto updateOrder(@PathVariable @Positive(message = "ID must be greater than 0") Long id,
                                @Valid @RequestBody OrderDto orderDto) {
        return orderService.update(id, orderDto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete order")
    public void deleteOrder(@PathVariable @Positive(message = "ID must be greater than 0") Long id) {
        orderService.deleteById(id);
    }
}
