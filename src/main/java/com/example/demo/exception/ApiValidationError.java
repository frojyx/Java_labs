package com.example.demo.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ошибка валидации поля")
public class ApiValidationError {
    @Schema(description = "Имя поля", example = "name")
    private String field;

    @Schema(description = "Сообщение об ошибке", example = "Название обязательно")
    private String message;

    public ApiValidationError() {
    }

    public ApiValidationError(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
