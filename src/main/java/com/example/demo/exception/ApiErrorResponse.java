package com.example.demo.exception;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Единый формат ошибки API")
public class ApiErrorResponse {
    @Schema(description = "Время ошибки", example = "2026-04-01T12:30:45")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP статус", example = "400")
    private int status;

    @Schema(description = "Краткое имя ошибки", example = "Bad Request")
    private String error;

    @Schema(description = "Сообщение об ошибке", example = "Ошибка валидации входных данных")
    private String message;

    @Schema(description = "Путь запроса", example = "/api/dish")
    private String path;

    @ArraySchema(schema = @Schema(description = "Ошибки полей"))
    private List<ApiValidationError> validationErrors;

    public ApiErrorResponse() {
    }

    public ApiErrorResponse(LocalDateTime timestamp, int status, String error,
                            String message, String path, List<ApiValidationError> validationErrors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.validationErrors = validationErrors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<ApiValidationError> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<ApiValidationError> validationErrors) {
        this.validationErrors = validationErrors;
    }
}
