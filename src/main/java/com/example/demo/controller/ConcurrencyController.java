package com.example.demo.controller;

import com.example.demo.dto.AsyncOrderRequestDto;
import com.example.demo.dto.AsyncTaskOverviewDto;
import com.example.demo.dto.AsyncTaskStatusDto;
import com.example.demo.dto.AsyncTaskSubmissionDto;
import com.example.demo.dto.RaceConditionDemoResultDto;
import com.example.demo.service.AsyncOrderSubmissionService;
import com.example.demo.service.AsyncOrderTaskService;
import com.example.demo.service.RaceConditionDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/concurrency")
@Tag(name = "Concurrency", description = "Async tasks, counters and race condition demos")
public class ConcurrencyController {
    private final AsyncOrderSubmissionService asyncOrderSubmissionService;
    private final AsyncOrderTaskService asyncOrderTaskService;
    private final RaceConditionDemoService raceConditionDemoService;

    public ConcurrencyController(AsyncOrderSubmissionService asyncOrderSubmissionService,
                                 AsyncOrderTaskService asyncOrderTaskService,
                                 RaceConditionDemoService raceConditionDemoService) {
        this.asyncOrderSubmissionService = asyncOrderSubmissionService;
        this.asyncOrderTaskService = asyncOrderTaskService;
        this.raceConditionDemoService = raceConditionDemoService;
    }

    @PostMapping("/orders")
    @Operation(summary = "Start asynchronous bulk order processing")
    public AsyncTaskSubmissionDto submitAsyncOrderTask(@Valid @RequestBody AsyncOrderRequestDto requestDto) {
        return asyncOrderSubmissionService.submitBulkOrderTask(requestDto);
    }

    @GetMapping("/orders")
    @Operation(summary = "Get overview for all asynchronous tasks")
    public AsyncTaskOverviewDto getAllTaskStatuses() {
        return asyncOrderTaskService.getAllTaskStatuses();
    }

    @GetMapping("/orders/{taskId}")
    @Operation(summary = "Get asynchronous task status by task ID")
    public AsyncTaskStatusDto getTaskStatus(@PathVariable @Positive(message = "Task ID must be greater than 0")
                                            Long taskId) {
        return asyncOrderTaskService.getTaskStatus(taskId);
    }

    @GetMapping("/race-demo")
    @Operation(summary = "Demonstrate a race condition and synchronized fix")
    public RaceConditionDemoResultDto runRaceConditionDemo(
        @RequestParam(defaultValue = "64")
        @Min(value = 3, message = "Thread count must be greater than 2")
        @Max(value = 200, message = "Thread count must not exceed 200")
        int threads,
        @RequestParam(defaultValue = "5000")
        @Positive(message = "Increments per thread must be greater than 0")
        int incrementsPerThread
    ) {
        return raceConditionDemoService.runDemo(threads, incrementsPerThread);
    }
}
