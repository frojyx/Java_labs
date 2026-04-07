package com.example.demo;

import com.example.demo.dto.ClientDto;
import com.example.demo.entity.Client;
import com.example.demo.entity.Order;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.ClientMapper;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {
    @Mock
    private ClientRepository clientRepository;

    @Mock
    private OrderRepository orderRepository;

    private ClientService clientService;

    @BeforeEach
    void initService() {
        clientService = new ClientService(clientRepository, new ClientMapper(), orderRepository);
    }

    @Test
    void findAllReturnsMappedDtos() {
        when(clientRepository.findAll()).thenReturn(List.of(new Client()));

        List<ClientDto> result = clientService.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void findByIdReturnsDto() {
        Client client = new Client();
        client.setId(3L);
        client.setFirstName("Ann");
        client.setLastName("Lee");
        when(clientRepository.findById(3L)).thenReturn(Optional.of(client));

        ClientDto result = clientService.findById(3L);

        assertEquals(3L, result.getId());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(clientRepository.findById(3L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> clientService.findById(3L));
    }

    @Test
    void saveStoresMappedEntity() {
        ClientDto dto = new ClientDto();
        dto.setFirstName("A");
        dto.setLastName("B");
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClientDto result = clientService.save(dto);

        assertEquals("A", result.getFirstName());
    }

    @Test
    void updateThrowsWhenMissing() {
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());
        ClientDto clientDto = new ClientDto();
        assertThrows(ResourceNotFoundException.class, () -> clientService.update(1L, clientDto));
    }

    @Test
    void updateChangesFieldsAndSaves() {
        Client client = new Client();
        client.setFirstName("Old");
        client.setLastName("Old");
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.save(client)).thenReturn(client);
        ClientDto dto = new ClientDto();
        dto.setFirstName("New");
        dto.setLastName("Name");

        ClientDto result = clientService.update(1L, dto);

        assertEquals("New", result.getFirstName());
        assertEquals("Name", result.getLastName());
    }

    @Test
    void deleteByIdThrowsWhenMissing() {
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> clientService.deleteById(1L));
    }

    @Test
    void deleteByIdSkipsOrderDeleteWhenNoOrders() {
        Client client = new Client();
        client.setOrders(List.of());
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        clientService.deleteById(1L);

        verify(orderRepository, never()).deleteAll(any());
        verify(clientRepository).delete(client);
    }

    @Test
    void deleteByIdSkipsOrderDeleteWhenOrdersNull() {
        Client client = new Client();
        client.setOrders(null);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        clientService.deleteById(1L);

        verify(orderRepository, never()).deleteAll(any());
        verify(clientRepository).delete(client);
    }

    @Test
    void deleteByIdDeletesOrdersWhenPresent() {
        Client client = new Client();
        client.setOrders(new ArrayList<>(List.of(new Order())));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        clientService.deleteById(1L);

        verify(orderRepository).deleteAll(client.getOrders());
        verify(clientRepository).delete(client);
    }
}
