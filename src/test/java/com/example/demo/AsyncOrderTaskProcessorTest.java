package com.example.demo;

import com.example.demo.dto.OrderDto;
import com.example.demo.service.AsyncOrderTaskProcessor;
import com.example.demo.service.AsyncOrderTaskService;
import com.example.demo.service.OrderService;
import com.example.demo.service.ThreadSafeMetricsService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class AsyncOrderTaskProcessorTest {
    @Test
    void processBulkOrderTaskMarksTaskCompleted() {
        StubOrderService orderService = new StubOrderService();
        RecordingAsyncOrderTaskService asyncOrderTaskService = new RecordingAsyncOrderTaskService();
        AsyncOrderTaskProcessor processor = new AsyncOrderTaskProcessor(orderService, asyncOrderTaskService);
        OrderDto orderDto = buildOrderDto();

        processor.processBulkOrderTask(5L, List.of(orderDto), 0L);

        assertEquals(List.of("RUNNING:5", "PROCESSED:5", "COMPLETED:5"), asyncOrderTaskService.events);
        assertEquals("Anna", asyncOrderTaskService.completedOrders.getFirst().getClientFirstName());
    }

    @Test
    void processBulkOrderTaskCompletesAfterPositiveDelay() {
        StubOrderService orderService = new StubOrderService();
        RecordingAsyncOrderTaskService asyncOrderTaskService = new RecordingAsyncOrderTaskService();
        AsyncOrderTaskProcessor processor = new AsyncOrderTaskProcessor(orderService, asyncOrderTaskService);

        processor.processBulkOrderTask(8L, List.of(buildOrderDto()), 1L);

        assertEquals(List.of("RUNNING:8", "PROCESSED:8", "COMPLETED:8"), asyncOrderTaskService.events);
    }

    @Test
    void processBulkOrderTaskMarksTaskFailedWhenBusinessOperationThrows() {
        StubOrderService orderService = new StubOrderService();
        orderService.failWith = new IllegalStateException("db down");
        RecordingAsyncOrderTaskService asyncOrderTaskService = new RecordingAsyncOrderTaskService();
        AsyncOrderTaskProcessor processor = new AsyncOrderTaskProcessor(orderService, asyncOrderTaskService);
        OrderDto orderDto = buildOrderDto();

        processor.processBulkOrderTask(6L, List.of(orderDto), 0L);

        assertEquals(List.of("RUNNING:6", "FAILED:6"), asyncOrderTaskService.events);
        assertEquals("db down", asyncOrderTaskService.failure.getMessage());
    }

    @Test
    void processBulkOrderTaskMarksTaskFailedWhenInterruptedDuringDelay() {
        StubOrderService orderService = new StubOrderService();
        RecordingAsyncOrderTaskService asyncOrderTaskService = new RecordingAsyncOrderTaskService();
        AsyncOrderTaskProcessor processor = new AsyncOrderTaskProcessor(orderService, asyncOrderTaskService);

        Thread.currentThread().interrupt();
        try {
            processor.processBulkOrderTask(7L, List.of(buildOrderDto()), 1L);
        } finally {
            Thread.interrupted();
        }

        assertEquals(List.of("RUNNING:7", "PROCESSED:7", "FAILED:7"), asyncOrderTaskService.events);
        assertInstanceOf(IllegalStateException.class, asyncOrderTaskService.failure);
    }

    private OrderDto buildOrderDto() {
        OrderDto orderDto = new OrderDto();
        orderDto.setClientFirstName("Anna");
        orderDto.setClientLastName("Lee");
        orderDto.setDishNames(List.of("Pasta"));
        return orderDto;
    }

    private static final class RecordingAsyncOrderTaskService extends AsyncOrderTaskService {
        private final List<String> events = new ArrayList<>();
        private List<OrderDto> completedOrders = List.of();
        private Exception failure;

        private RecordingAsyncOrderTaskService() {
            super(new ThreadSafeMetricsService());
        }

        @Override
        public void markTaskAsRunning(long taskId) {
            events.add("RUNNING:" + taskId);
        }

        @Override
        public void markOrderProcessed(long taskId) {
            events.add("PROCESSED:" + taskId);
        }

        @Override
        public void markTaskAsCompleted(long taskId, List<OrderDto> createdOrders) {
            completedOrders = createdOrders;
            events.add("COMPLETED:" + taskId);
        }

        @Override
        public void markTaskAsFailed(long taskId, Exception exception) {
            failure = exception;
            events.add("FAILED:" + taskId);
        }
    }

    private static class StubOrderService extends OrderService {
        private RuntimeException failWith;

        private StubOrderService() {
            super(null, null, null, null);
        }

        @Override
        public OrderDto createNewOrder(OrderDto orderDto) {
            if (failWith != null) {
                throw failWith;
            }
            return orderDto;
        }
    }
}
