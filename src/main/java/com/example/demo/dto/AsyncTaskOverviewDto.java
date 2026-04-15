package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Overview for all asynchronous tasks")
public class AsyncTaskOverviewDto {
    @Schema(description = "Total number of async tasks", example = "6")
    private int totalTasks;

    @Schema(description = "Tasks waiting to start", example = "0")
    private int pendingTasks;

    @Schema(description = "Tasks currently running", example = "1")
    private int runningTasks;

    @Schema(description = "Successfully completed tasks", example = "5")
    private int completedTasks;

    @Schema(description = "Failed tasks", example = "0")
    private int failedTasks;

    @ArraySchema(schema = @Schema(implementation = AsyncTaskStatusDto.class))
    private List<AsyncTaskStatusDto> tasks;

    public int getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(int totalTasks) {
        this.totalTasks = totalTasks;
    }

    public int getPendingTasks() {
        return pendingTasks;
    }

    public void setPendingTasks(int pendingTasks) {
        this.pendingTasks = pendingTasks;
    }

    public int getRunningTasks() {
        return runningTasks;
    }

    public void setRunningTasks(int runningTasks) {
        this.runningTasks = runningTasks;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(int completedTasks) {
        this.completedTasks = completedTasks;
    }

    public int getFailedTasks() {
        return failedTasks;
    }

    public void setFailedTasks(int failedTasks) {
        this.failedTasks = failedTasks;
    }

    public List<AsyncTaskStatusDto> getTasks() {
        return tasks;
    }

    public void setTasks(List<AsyncTaskStatusDto> tasks) {
        this.tasks = tasks;
    }
}
