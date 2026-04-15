package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Current state of an asynchronous order task")
public class AsyncTaskStatusDto {
    @Schema(description = "Task identifier", example = "42")
    private Long taskId;

    @Schema(description = "Task status", example = "COMPLETED")
    private String status;

    @Schema(description = "How many orders were submitted", example = "3")
    private int totalOrders;

    @Schema(description = "How many orders were processed", example = "3")
    private int processedOrders;

    @Schema(description = "Task creation time", example = "2026-04-14T15:20:00")
    private LocalDateTime createdAt;

    @Schema(description = "Task start time", example = "2026-04-14T15:20:01")
    private LocalDateTime startedAt;

    @Schema(description = "Task finish time", example = "2026-04-14T15:20:03")
    private LocalDateTime finishedAt;

    @Schema(description = "Error details for failed tasks", example = "Dish list is required")
    private String errorMessage;

    @ArraySchema(schema = @Schema(implementation = OrderDto.class))
    private List<OrderDto> createdOrders;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public int getProcessedOrders() {
        return processedOrders;
    }

    public void setProcessedOrders(int processedOrders) {
        this.processedOrders = processedOrders;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<OrderDto> getCreatedOrders() {
        return createdOrders;
    }

    public void setCreatedOrders(List<OrderDto> createdOrders) {
        this.createdOrders = createdOrders;
    }
}
