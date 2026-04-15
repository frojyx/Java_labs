package com.example.demo;

import com.example.demo.dto.AsyncOrderRequestDto;
import com.example.demo.dto.AsyncTaskSubmissionDto;
import com.example.demo.dto.OrderDto;
import com.example.demo.service.AsyncOrderSubmissionService;
import com.example.demo.service.AsyncOrderTaskProcessor;
import com.example.demo.service.AsyncOrderTaskService;
import com.example.demo.service.OrderService;
import com.example.demo.service.ThreadSafeMetricsService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AsyncOrderSubmissionServiceTest {
    @Test
    void submitBulkOrderTaskCreatesTaskAndDelegatesProcessing() {
        AsyncOrderTaskService taskService = new AsyncOrderTaskService(new ThreadSafeMetricsService());
        RecordingProcessor asyncOrderTaskProcessor = new RecordingProcessor();
        AsyncOrderSubmissionService submissionService =
            new AsyncOrderSubmissionService(taskService, asyncOrderTaskProcessor);
        AsyncOrderRequestDto requestDto = buildRequest();

        AsyncTaskSubmissionDto submissionDto = submissionService.submitBulkOrderTask(requestDto);

        assertEquals("PENDING", submissionDto.getStatus());
        assertEquals(submissionDto.getTaskId(), asyncOrderTaskProcessor.taskId);
        assertEquals(requestDto.getOrders(), asyncOrderTaskProcessor.orders);
        assertEquals(requestDto.getDelayMillis(), asyncOrderTaskProcessor.delayMillis);
    }

    private AsyncOrderRequestDto buildRequest() {
        AsyncOrderRequestDto requestDto = new AsyncOrderRequestDto();
        OrderDto orderDto = new OrderDto();
        orderDto.setClientFirstName("Anna");
        orderDto.setClientLastName("Lee");
        orderDto.setDishNames(List.of("Pasta"));
        requestDto.setOrders(List.of(orderDto));
        requestDto.setDelayMillis(0L);
        return requestDto;
    }

    private static final class RecordingProcessor extends AsyncOrderTaskProcessor {
        private Long taskId;
        private List<OrderDto> orders;
        private long delayMillis;

        private RecordingProcessor() {
            super(new StubOrderService(), new AsyncOrderTaskService(new ThreadSafeMetricsService()));
        }

        @Override
        public CompletableFuture<Void> processBulkOrderTask(long taskId, List<OrderDto> orders, long delayMillis) {
            this.taskId = taskId;
            this.orders = orders;
            this.delayMillis = delayMillis;
            return CompletableFuture.completedFuture(null);
        }
    }

    private static class StubOrderService extends OrderService {
        private StubOrderService() {
            super(null, null, null, null);
        }
    }
}
