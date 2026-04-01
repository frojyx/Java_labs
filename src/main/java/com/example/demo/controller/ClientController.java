package com.example.demo.controller;

import com.example.demo.dto.ClientDto;
import com.example.demo.service.ClientService;
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
@RequestMapping("/api/clients")
@Tag(name = "Clients", description = "Операции с клиентами")
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    @Operation(summary = "Получить всех клиентов")
    public List<ClientDto> getAllClients() {
        return clientService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить клиента по ID")
    public ClientDto getClientById(@PathVariable @Positive(message = "ID должен быть больше 0") Long id) {
        return clientService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Создать клиента")
    public ClientDto createClient(@Valid @RequestBody ClientDto clientDto) {
        return clientService.save(clientDto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить клиента")
    public ClientDto updateClient(@PathVariable @Positive(message = "ID должен быть больше 0") Long id,
                                  @Valid @RequestBody ClientDto clientDto) {
        return clientService.update(id, clientDto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить клиента")
    public void deleteClient(@PathVariable @Positive(message = "ID должен быть больше 0") Long id) {
        clientService.deleteById(id);
    }
}
