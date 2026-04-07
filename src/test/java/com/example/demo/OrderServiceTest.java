package com.example.demo;

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
import com.example.demo.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private DishRepository dishRepository;

    private OrderService orderService;

    @BeforeEach
    void initService() {
        orderService = new OrderService(orderRepository, clientRepository, new OrderMapper(), dishRepository);
    }

    @Test
    void findAllReturnsMappedOrders() {
        when(orderRepository.findAll()).thenReturn(List.of(new Order()));

        List<OrderDto> result = orderService.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void findByIdReturnsOrder() {
        Order order = new Order();
        order.setId(10L);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        OrderDto result = orderService.findById(10L);

        assertEquals(10L, result.getId());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(orderRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.findById(10L));
    }

    @Test
    void createNewOrderCreatesClientAndOrder() {
        OrderDto orderDto = buildOrderDto("Anna", "Lee", List.of("Pasta"));
        Dish pasta = buildDish(1L, "Pasta");

        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dishRepository.findByNameIn(List.of("Pasta"))).thenReturn(List.of(pasta));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(55L);
            return order;
        });

        OrderDto result = orderService.createNewOrder(orderDto);

        assertEquals(55L, result.getId());
        verify(clientRepository).save(any(Client.class));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createNewOrderThrowsWhenDishNamesMissing() {
        OrderDto orderDto = buildOrderDto("Anna", "Lee", null);

        assertThrows(BadRequestException.class, () -> orderService.createNewOrder(orderDto));
    }

    @Test
    void createNewOrderThrowsWhenDishNameBlank() {
        OrderDto orderDto = buildOrderDto("Anna", "Lee", List.of(" "));

        assertThrows(BadRequestException.class, () -> orderService.createNewOrder(orderDto));
    }

    @Test
    void createNewOrderThrowsWhenDishNamesDuplicated() {
        OrderDto orderDto = buildOrderDto("Anna", "Lee", List.of("Pasta", "Pasta"));

        assertThrows(BadRequestException.class, () -> orderService.createNewOrder(orderDto));
    }

    @Test
    void createNewOrderThrowsWhenDishNotFound() {
        OrderDto orderDto = buildOrderDto("Anna", "Lee", List.of("Pasta", "Soup"));
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dishRepository.findByNameIn(List.of("Pasta", "Soup"))).thenReturn(List.of(buildDish(1L, "Pasta")));

        assertThrows(UnprocessableEntityException.class, () -> orderService.createNewOrder(orderDto));
    }

    @Test
    void createOrdersBulkReturnsCreatedOrders() {
        OrderDto first = buildOrderDto("A", "B", List.of("Pasta"));
        OrderDto second = buildOrderDto("C", "D", List.of("Soup"));

        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dishRepository.findByNameIn(List.of("Pasta"))).thenReturn(List.of(buildDish(1L, "Pasta")));
        when(dishRepository.findByNameIn(List.of("Soup"))).thenReturn(List.of(buildDish(2L, "Soup")));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(order.getClient().getFirstName().equals("A") ? 1L : 2L);
            return order;
        });

        List<OrderDto> result = orderService.createOrdersBulk(List.of(first, second));

        assertEquals(2, result.size());
        verify(orderRepository, times(2)).save(any(Order.class));
    }

    @Test
    void createOrdersBulkThrowsForEmptyPayload() {
        assertThrows(BadRequestException.class, () -> orderService.createOrdersBulk(List.of()));
        assertThrows(BadRequestException.class, () -> orderService.createOrdersBulk(null));
    }

    @Test
    void createOrdersBulkWithoutTransactionDemoThrowsAfterFirstSave() {
        OrderDto first = buildOrderDto("A", "B", List.of("Pasta"));
        OrderDto second = buildOrderDto("C", "D", List.of("Soup"));
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dishRepository.findByNameIn(List.of("Pasta"))).thenReturn(List.of(buildDish(1L, "Pasta")));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(RuntimeException.class,
            () -> orderService.createOrdersBulkWithoutTransactionDemo(List.of(first, second)));
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrdersBulkWithoutTransactionDemoReturnsWhenSingleOrder() {
        OrderDto first = buildOrderDto("A", "B", List.of("Pasta"));
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dishRepository.findByNameIn(List.of("Pasta"))).thenReturn(List.of(buildDish(1L, "Pasta")));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(77L);
            return order;
        });

        List<OrderDto> result = orderService.createOrdersBulkWithoutTransactionDemo(List.of(first));

        assertEquals(1, result.size());
        assertEquals(77L, result.get(0).getId());
    }

    @Test
    void createOrdersBulkWithTransactionDemoThrowsAfterFirstSave() {
        OrderDto first = buildOrderDto("A", "B", List.of("Pasta"));
        OrderDto second = buildOrderDto("C", "D", List.of("Soup"));
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dishRepository.findByNameIn(List.of("Pasta"))).thenReturn(List.of(buildDish(1L, "Pasta")));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(RuntimeException.class,
            () -> orderService.createOrdersBulkWithTransactionDemo(List.of(first, second)));
    }

    @Test
    void createOrdersBulkWithTransactionDemoReturnsWhenSingleOrder() {
        OrderDto first = buildOrderDto("A", "B", List.of("Pasta"));
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dishRepository.findByNameIn(List.of("Pasta"))).thenReturn(List.of(buildDish(1L, "Pasta")));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(88L);
            return order;
        });

        List<OrderDto> result = orderService.createOrdersBulkWithTransactionDemo(List.of(first));

        assertEquals(1, result.size());
        assertEquals(88L, result.get(0).getId());
    }

    @Test
    void updateChangesClientDataWhenPresent() {
        Order order = new Order();
        Client client = new Client();
        client.setFirstName("Old");
        client.setLastName("Name");
        order.setClient(client);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderDto update = buildOrderDto("New", "Client", List.of("Pasta"));
        OrderDto result = orderService.update(1L, update);

        assertEquals("New", result.getClientFirstName());
        assertEquals("Client", result.getClientLastName());
    }

    @Test
    void updateSavesOrderWhenClientIsNull() {
        Order order = new Order();
        order.setClient(null);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderDto update = buildOrderDto("New", "Client", List.of("Pasta"));
        orderService.update(1L, update);

        verify(orderRepository).save(order);
    }

    @Test
    void updateThrowsWhenOrderMissing() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        OrderDto update = buildOrderDto("New", "Client", List.of("Pasta"));

        assertThrows(ResourceNotFoundException.class, () -> orderService.update(1L, update));
    }

    @Test
    void deleteByIdDeletesClientWhenNoOrdersLeft() {
        Order order = new Order();
        order.setId(5L);
        Client client = new Client();
        client.setOrders(new ArrayList<>(List.of(order)));
        order.setClient(client);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        doNothing().when(orderRepository).delete(order);

        orderService.deleteById(5L);

        verify(orderRepository).delete(order);
        verify(clientRepository).delete(client);
    }

    @Test
    void deleteByIdDoesNotDeleteClientWhenStillHasOrders() {
        Order order = new Order();
        order.setId(5L);
        Order another = new Order();
        another.setId(6L);
        Client client = new Client();
        client.setOrders(new ArrayList<>(List.of(order, another)));
        order.setClient(client);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        orderService.deleteById(5L);

        verify(clientRepository, never()).delete(any(Client.class));
    }

    @Test
    void deleteByIdSkipsClientCleanupWhenClientOrOrdersNull() {
        Order withNullClient = new Order();
        withNullClient.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(withNullClient));
        orderService.deleteById(1L);

        Order withNullOrders = new Order();
        withNullOrders.setId(2L);
        Client client = new Client();
        client.setOrders(null);
        withNullOrders.setClient(client);
        when(orderRepository.findById(2L)).thenReturn(Optional.of(withNullOrders));
        orderService.deleteById(2L);

        verify(clientRepository, never()).delete(any(Client.class));
    }

    @Test
    void deleteByIdThrowsWhenMissing() {
        when(orderRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderService.deleteById(5L));
    }

    @Test
    void createNewOrderTrimsDishNamesBeforeLookup() {
        OrderDto orderDto = buildOrderDto("A", "B", List.of("  Pasta  "));
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dishRepository.findByNameIn(List.of("Pasta"))).thenReturn(List.of(buildDish(1L, "Pasta")));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.createNewOrder(orderDto);

        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(dishRepository).findByNameIn(captor.capture());
        assertEquals("Pasta", captor.getValue().get(0));
    }

    @Test
    void createNewOrderThrowsWhenDishNameIsNull() {
        List<String> dishNames = new ArrayList<>();
        dishNames.add(null);
        OrderDto orderDto = buildOrderDto("A", "B", dishNames);

        assertThrows(BadRequestException.class, () -> orderService.createNewOrder(orderDto));
    }

    private OrderDto buildOrderDto(String firstName, String lastName, List<String> dishes) {
        OrderDto dto = new OrderDto();
        dto.setClientFirstName(firstName);
        dto.setClientLastName(lastName);
        dto.setDishNames(dishes);
        return dto;
    }

    private Dish buildDish(Long id, String name) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setName(name);
        return dish;
    }
}
