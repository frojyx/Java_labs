package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thread-safe metrics snapshot")
public class ConcurrencyMetricsDto {
    @Schema(description = "How many asynchronous tasks finished successfully", example = "5")
    private int completedAsyncTasks;

    public ConcurrencyMetricsDto() {
    }

    public ConcurrencyMetricsDto(int completedAsyncTasks) {
        this.completedAsyncTasks = completedAsyncTasks;
    }

    public int getCompletedAsyncTasks() {
        return completedAsyncTasks;
    }

    public void setCompletedAsyncTasks(int completedAsyncTasks) {
        this.completedAsyncTasks = completedAsyncTasks;
    }
}
