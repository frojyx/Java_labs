package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Request payload for asynchronous order processing")
public class AsyncOrderRequestDto {
    @Valid
    @NotEmpty(message = "Order list is required")
    @ArraySchema(schema = @Schema(implementation = OrderDto.class))
    private List<OrderDto> orders;

    @Min(value = 0, message = "Delay must not be negative")
    @Schema(description = "Artificial delay between orders in milliseconds", example = "150")
    private long delayMillis = 150L;

    public List<OrderDto> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderDto> orders) {
        this.orders = orders;
    }

    public long getDelayMillis() {
        return delayMillis;
    }

    public void setDelayMillis(long delayMillis) {
        this.delayMillis = delayMillis;
    }
}
