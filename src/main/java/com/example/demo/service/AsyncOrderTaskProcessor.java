package com.example.demo.service;

import com.example.demo.dto.OrderDto;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncOrderTaskProcessor {
    private final OrderService orderService;
    private final AsyncOrderTaskService asyncOrderTaskService;

    public AsyncOrderTaskProcessor(OrderService orderService, AsyncOrderTaskService asyncOrderTaskService) {
        this.orderService = orderService;
        this.asyncOrderTaskService = asyncOrderTaskService;
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> processBulkOrderTask(long taskId, List<OrderDto> orders, long delayMillis) {
        asyncOrderTaskService.markTaskAsRunning(taskId);

        try {
            List<OrderDto> createdOrders = new ArrayList<>();
            for (OrderDto orderDto : orders) {
                createdOrders.add(orderService.createNewOrder(orderDto));
                asyncOrderTaskService.markOrderProcessed(taskId);
                pauseBetweenOperations(delayMillis);
            }
            asyncOrderTaskService.markTaskAsCompleted(taskId, createdOrders);
        } catch (Exception exception) {
            asyncOrderTaskService.markTaskAsFailed(taskId, exception);
        }

        return CompletableFuture.completedFuture(null);
    }

    private void pauseBetweenOperations(long delayMillis) {
        if (delayMillis <= 0) {
            return;
        }

        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Async order task was interrupted", exception);
        }
    }
}
