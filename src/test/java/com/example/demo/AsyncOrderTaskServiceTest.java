package com.example.demo;

import com.example.demo.dto.AsyncOrderRequestDto;
import com.example.demo.dto.AsyncTaskOverviewDto;
import com.example.demo.dto.AsyncTaskStatusDto;
import com.example.demo.dto.AsyncTaskSubmissionDto;
import com.example.demo.dto.OrderDto;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.AsyncOrderTaskService;
import com.example.demo.service.ThreadSafeMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AsyncOrderTaskServiceTest {
    private AsyncOrderTaskService asyncOrderTaskService;

    @BeforeEach
    void setUp() {
        asyncOrderTaskService = new AsyncOrderTaskService(new ThreadSafeMetricsService());
    }

    @Test
    void createTaskReturnsPendingStatusAndStoresTask() {
        AsyncTaskSubmissionDto submissionDto = asyncOrderTaskService.createTask(buildRequest());

        AsyncTaskStatusDto statusDto = asyncOrderTaskService.getTaskStatus(submissionDto.getTaskId());

        assertEquals("PENDING", submissionDto.getStatus());
        assertEquals(2, statusDto.getTotalOrders());
        assertEquals(0, statusDto.getProcessedOrders());
        assertNotNull(statusDto.getCreatedAt());
    }

    @Test
    void getTaskStatusThrowsWhenTaskMissing() {
        assertThrows(ResourceNotFoundException.class, () -> asyncOrderTaskService.getTaskStatus(999L));
    }

    @Test
    void markTaskLifecycleUpdatesStoredState() {
        AsyncTaskSubmissionDto submissionDto = asyncOrderTaskService.createTask(buildRequest());

        asyncOrderTaskService.markTaskAsRunning(submissionDto.getTaskId());
        asyncOrderTaskService.markOrderProcessed(submissionDto.getTaskId());
        asyncOrderTaskService.markTaskAsCompleted(submissionDto.getTaskId(), List.of(buildOrderDto("Done")));

        AsyncTaskStatusDto statusDto = asyncOrderTaskService.getTaskStatus(submissionDto.getTaskId());
        assertEquals("COMPLETED", statusDto.getStatus());
        assertEquals(1, statusDto.getProcessedOrders());
        assertEquals(1, statusDto.getCreatedOrders().size());
        assertNotNull(statusDto.getStartedAt());
        assertNotNull(statusDto.getFinishedAt());
    }

    @Test
    void markTaskAsFailedStoresErrorMessage() {
        AsyncTaskSubmissionDto submissionDto = asyncOrderTaskService.createTask(buildRequest());

        asyncOrderTaskService.markTaskAsFailed(submissionDto.getTaskId(), new IllegalStateException("boom"));

        AsyncTaskStatusDto statusDto = asyncOrderTaskService.getTaskStatus(submissionDto.getTaskId());
        assertEquals("FAILED", statusDto.getStatus());
        assertEquals("boom", statusDto.getErrorMessage());
        assertNotNull(statusDto.getFinishedAt());
    }

    @Test
    void markMethodsThrowWhenTaskMissing() {
        Executable markOrderProcessed = () -> asyncOrderTaskService.markOrderProcessed(100L);
        Executable markTaskAsCompleted = () -> asyncOrderTaskService.markTaskAsCompleted(100L, List.of());
        Executable markTaskAsFailed =
            () -> asyncOrderTaskService.markTaskAsFailed(100L, new IllegalStateException("missing"));

        assertThrows(ResourceNotFoundException.class, markOrderProcessed);
        assertThrows(ResourceNotFoundException.class, markTaskAsCompleted);
        assertThrows(ResourceNotFoundException.class, markTaskAsFailed);
    }

    @Test
    void markTaskAsRunningIgnoresUnknownTask() {
        asyncOrderTaskService.markTaskAsRunning(200L);

        AsyncTaskOverviewDto overviewDto = asyncOrderTaskService.getAllTaskStatuses();
        assertEquals(0, overviewDto.getTotalTasks());
    }

    @Test
    void getAllTaskStatusesReturnsSummaryAndTasks() {
        AsyncTaskSubmissionDto pendingTask = asyncOrderTaskService.createTask(buildRequest());
        AsyncTaskSubmissionDto completedTask = asyncOrderTaskService.createTask(buildRequest());
        AsyncTaskSubmissionDto failedTask = asyncOrderTaskService.createTask(buildRequest());

        asyncOrderTaskService.markTaskAsRunning(completedTask.getTaskId());
        asyncOrderTaskService.markTaskAsCompleted(completedTask.getTaskId(), List.of(buildOrderDto("Done")));
        asyncOrderTaskService.markTaskAsFailed(failedTask.getTaskId(), new IllegalStateException("boom"));

        AsyncTaskOverviewDto overviewDto = asyncOrderTaskService.getAllTaskStatuses();

        assertEquals(3, overviewDto.getTotalTasks());
        assertEquals(1, overviewDto.getPendingTasks());
        assertEquals(0, overviewDto.getRunningTasks());
        assertEquals(1, overviewDto.getCompletedTasks());
        assertEquals(1, overviewDto.getFailedTasks());
        assertEquals(3, overviewDto.getTasks().size());
        assertEquals(pendingTask.getTaskId(), overviewDto.getTasks().getFirst().getTaskId());
    }

    private AsyncOrderRequestDto buildRequest() {
        AsyncOrderRequestDto requestDto = new AsyncOrderRequestDto();
        requestDto.setOrders(List.of(buildOrderDto("A"), buildOrderDto("B")));
        requestDto.setDelayMillis(0L);
        return requestDto;
    }

    private OrderDto buildOrderDto(String firstName) {
        OrderDto orderDto = new OrderDto();
        orderDto.setClientFirstName(firstName);
        orderDto.setClientLastName("Client");
        orderDto.setDishNames(List.of("Pasta"));
        return orderDto;
    }
}
