package com.example.demo.service;

import com.example.demo.dto.ClientDto;
import com.example.demo.entity.Client;
import com.example.demo.entity.Order;
import com.example.demo.mapper.ClientMapper;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientService {
    private static final String CLIENT_WITH_ID_PREFIX = "Клиент с ID ";

    private static final String NOT_FOUND_SUFFIX = " не найден";

    private final ClientRepository clientRepository;

    private final ClientMapper clientMapper;

    private final OrderRepository orderRepository;

    public ClientService(ClientRepository clientRepository, ClientMapper clientMapper,
                         OrderRepository orderRepository) {
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<ClientDto> findAll() {
        return clientMapper.toDtoList(clientRepository.findAll());
    }

    @Transactional(readOnly = true)
    public ClientDto findById(Long id) {
        return clientRepository.findById(id)
            .map(clientMapper::toDto)
            .orElseThrow(() -> new RuntimeException(CLIENT_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX));
    }

    @Transactional
    public ClientDto save(ClientDto clientDto) {
        Client client = clientMapper.toEntity(clientDto);
        return clientMapper.toDto(clientRepository.save(client));
    }

    @Transactional
    public ClientDto update(Long id, ClientDto clientDto) {
        Client client = clientRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(CLIENT_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX));
        client.setFirstName(clientDto.getFirstName());
        client.setLastName(clientDto.getLastName());
        return clientMapper.toDto(clientRepository.save(client));
    }

    @Transactional
    public void deleteById(Long id) {
        Client client = clientRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(CLIENT_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX));

        List<Order> orders = client.getOrders();
        if (orders != null && !orders.isEmpty()) {
            orderRepository.deleteAll(orders);
        }

        clientRepository.delete(client);
    }
}
