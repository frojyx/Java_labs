package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO клиента")
public class ClientDto {
    @Schema(description = "Идентификатор клиента", example = "10")
    private Long id;

    @NotBlank(message = "Имя клиента обязательно")
    @Size(max = 100, message = "Имя клиента не должно превышать 100 символов")
    @Schema(description = "Имя клиента", example = "Иван")
    private String firstName;

    @NotBlank(message = "Фамилия клиента обязательна")
    @Size(max = 100, message = "Фамилия клиента не должна превышать 100 символов")
    @Schema(description = "Фамилия клиента", example = "Петров")
    private String lastName;

    public ClientDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
