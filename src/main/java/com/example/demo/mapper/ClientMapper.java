package com.example.demo.mapper;

import com.example.demo.dto.ClientDto;
import com.example.demo.entity.Client;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ClientMapper {
    public ClientDto toDto(Client client) {
        ClientDto dto = new ClientDto();
        dto.setId(client.getId());
        dto.setFirstName(client.getFirstName());
        dto.setLastName(client.getLastName());
        return dto;
    }

    public Client toEntity(ClientDto clientDto) {
        Client client = new Client();
        client.setFirstName(clientDto.getFirstName());
        client.setLastName(clientDto.getLastName());
        return client;
    }

    public List<ClientDto> toDtoList(List<Client> clients) {
        List<ClientDto> list = new ArrayList<>();
        for (Client client : clients) {
            list.add(toDto(client));
        }
        return list;
    }
}
