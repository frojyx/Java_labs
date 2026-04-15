package com.example.demo.service;

import com.example.demo.dto.OrderDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AsyncOrderTaskState {
    private final Long taskId;
    private final int totalOrders;
    private final LocalDateTime createdAt;
    private final AtomicInteger processedOrders;
    private final AtomicReference<List<OrderDto>> createdOrders;
    private volatile AsyncTaskStatus status;
    private volatile LocalDateTime startedAt;
    private volatile LocalDateTime finishedAt;
    private volatile String errorMessage;

    public AsyncOrderTaskState(Long taskId, int totalOrders) {
        this.taskId = taskId;
        this.totalOrders = totalOrders;
        this.createdAt = LocalDateTime.now();
        this.processedOrders = new AtomicInteger();
        this.status = AsyncTaskStatus.PENDING;
        this.createdOrders = new AtomicReference<>(List.of());
    }

    public Long getTaskId() {
        return taskId;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getProcessedOrders() {
        return processedOrders.get();
    }

    public int incrementProcessedOrders() {
        return processedOrders.incrementAndGet();
    }

    public AsyncTaskStatus getStatus() {
        return status;
    }

    public void setStatus(AsyncTaskStatus status) {
        this.status = status;
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
        return createdOrders.get();
    }

    public void setCreatedOrders(List<OrderDto> createdOrders) {
        this.createdOrders.set(List.copyOf(createdOrders));
    }
}
