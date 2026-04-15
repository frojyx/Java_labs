package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Async task submission response")
public class AsyncTaskSubmissionDto {
    @Schema(description = "Generated task identifier", example = "42")
    private Long taskId;

    @Schema(description = "Current task status", example = "PENDING")
    private String status;

    public AsyncTaskSubmissionDto() {
    }

    public AsyncTaskSubmissionDto(Long taskId, String status) {
        this.taskId = taskId;
        this.status = status;
    }

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
}
