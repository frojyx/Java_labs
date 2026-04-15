package com.example.demo.service;

import com.example.demo.dto.AsyncOrderRequestDto;
import com.example.demo.dto.AsyncTaskSubmissionDto;
import org.springframework.stereotype.Service;

@Service
public class AsyncOrderSubmissionService {
    private final AsyncOrderTaskService asyncOrderTaskService;
    private final AsyncOrderTaskProcessor asyncOrderTaskProcessor;

    public AsyncOrderSubmissionService(AsyncOrderTaskService asyncOrderTaskService,
                                       AsyncOrderTaskProcessor asyncOrderTaskProcessor) {
        this.asyncOrderTaskService = asyncOrderTaskService;
        this.asyncOrderTaskProcessor = asyncOrderTaskProcessor;
    }

    public AsyncTaskSubmissionDto submitBulkOrderTask(AsyncOrderRequestDto requestDto) {
        AsyncTaskSubmissionDto submissionDto = asyncOrderTaskService.createTask(requestDto);
        asyncOrderTaskProcessor.processBulkOrderTask(
            submissionDto.getTaskId(),
            requestDto.getOrders(),
            requestDto.getDelayMillis()
        );
        return submissionDto;
    }
}
