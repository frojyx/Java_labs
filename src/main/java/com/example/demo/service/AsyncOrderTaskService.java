package com.example.demo.service;

import com.example.demo.dto.AsyncOrderRequestDto;
import com.example.demo.dto.AsyncTaskOverviewDto;
import com.example.demo.dto.AsyncTaskStatusDto;
import com.example.demo.dto.AsyncTaskSubmissionDto;
import com.example.demo.dto.OrderDto;
import com.example.demo.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AsyncOrderTaskService {
    private final ConcurrentMap<Long, AsyncOrderTaskState> taskStorage = new ConcurrentHashMap<>();
    private final ThreadSafeMetricsService threadSafeMetricsService;

    public AsyncOrderTaskService(ThreadSafeMetricsService threadSafeMetricsService) {
        this.threadSafeMetricsService = threadSafeMetricsService;
    }

    public AsyncTaskSubmissionDto createTask(AsyncOrderRequestDto requestDto) {
        long taskId = threadSafeMetricsService.nextTaskId();
        AsyncOrderTaskState taskState = new AsyncOrderTaskState(taskId, requestDto.getOrders().size());
        taskStorage.put(taskId, taskState);
        return new AsyncTaskSubmissionDto(taskId, taskState.getStatus().name());
    }

    public AsyncTaskStatusDto getTaskStatus(long taskId) {
        AsyncOrderTaskState taskState = taskStorage.get(taskId);
        if (taskState == null) {
            throw new ResourceNotFoundException("Async task with ID " + taskId + " not found");
        }
        return mapToDto(taskState);
    }

    public AsyncTaskOverviewDto getAllTaskStatuses() {
        List<AsyncTaskStatusDto> tasks = taskStorage.values()
            .stream()
            .sorted(Comparator.comparing(AsyncOrderTaskState::getTaskId))
            .map(this::mapToDto)
            .toList();

        AsyncTaskOverviewDto overviewDto = new AsyncTaskOverviewDto();
        overviewDto.setTotalTasks(tasks.size());
        overviewDto.setPendingTasks(countByStatus(tasks, AsyncTaskStatus.PENDING));
        overviewDto.setRunningTasks(countByStatus(tasks, AsyncTaskStatus.RUNNING));
        overviewDto.setCompletedTasks(countByStatus(tasks, AsyncTaskStatus.COMPLETED));
        overviewDto.setFailedTasks(countByStatus(tasks, AsyncTaskStatus.FAILED));
        overviewDto.setTasks(tasks);
        return overviewDto;
    }

    public void markTaskAsRunning(long taskId) {
        AsyncOrderTaskState taskState = taskStorage.get(taskId);
        if (taskState == null) {
            return;
        }
        taskState.setStatus(AsyncTaskStatus.RUNNING);
        taskState.setStartedAt(LocalDateTime.now());
    }

    public void markOrderProcessed(long taskId) {
        AsyncOrderTaskState taskState = requireTaskState(taskId);
        taskState.incrementProcessedOrders();
    }

    public void markTaskAsCompleted(long taskId, List<OrderDto> createdOrders) {
        AsyncOrderTaskState taskState = requireTaskState(taskId);
        taskState.setCreatedOrders(List.copyOf(createdOrders));
        taskState.setStatus(AsyncTaskStatus.COMPLETED);
        taskState.setFinishedAt(LocalDateTime.now());
    }

    public void markTaskAsFailed(long taskId, Exception exception) {
        AsyncOrderTaskState taskState = requireTaskState(taskId);
        taskState.setStatus(AsyncTaskStatus.FAILED);
        taskState.setErrorMessage(exception.getMessage());
        taskState.setFinishedAt(LocalDateTime.now());
    }

    private AsyncTaskStatusDto mapToDto(AsyncOrderTaskState taskState) {
        AsyncTaskStatusDto dto = new AsyncTaskStatusDto();
        dto.setTaskId(taskState.getTaskId());
        dto.setStatus(taskState.getStatus().name());
        dto.setTotalOrders(taskState.getTotalOrders());
        dto.setProcessedOrders(taskState.getProcessedOrders());
        dto.setCreatedAt(taskState.getCreatedAt());
        dto.setStartedAt(taskState.getStartedAt());
        dto.setFinishedAt(taskState.getFinishedAt());
        dto.setErrorMessage(taskState.getErrorMessage());
        dto.setCreatedOrders(taskState.getCreatedOrders());
        return dto;
    }

    private AsyncOrderTaskState requireTaskState(long taskId) {
        AsyncOrderTaskState taskState = taskStorage.get(taskId);
        if (taskState == null) {
            throw new ResourceNotFoundException("Async task with ID " + taskId + " not found");
        }
        return taskState;
    }

    private int countByStatus(List<AsyncTaskStatusDto> tasks, AsyncTaskStatus status) {
        return (int) tasks.stream()
            .filter(task -> status.name().equals(task.getStatus()))
            .count();
    }
}
