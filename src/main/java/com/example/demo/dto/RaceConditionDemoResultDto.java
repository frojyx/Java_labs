package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of race condition demonstration")
public class RaceConditionDemoResultDto {
    @Schema(description = "Number of threads used in the demo", example = "64")
    private int threadCount;

    @Schema(description = "Increments per thread", example = "5000")
    private int incrementsPerThread;

    @Schema(description = "Expected final count", example = "320000")
    private int expectedCount;

    @Schema(description = "Unsafe counter result", example = "287143")
    private int unsafeActualCount;

    @Schema(description = "Synchronized counter result", example = "320000")
    private int synchronizedActualCount;

    @Schema(description = "Atomic counter result", example = "320000")
    private int atomicActualCount;

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public int getIncrementsPerThread() {
        return incrementsPerThread;
    }

    public void setIncrementsPerThread(int incrementsPerThread) {
        this.incrementsPerThread = incrementsPerThread;
    }

    public int getExpectedCount() {
        return expectedCount;
    }

    public void setExpectedCount(int expectedCount) {
        this.expectedCount = expectedCount;
    }

    public int getUnsafeActualCount() {
        return unsafeActualCount;
    }

    public void setUnsafeActualCount(int unsafeActualCount) {
        this.unsafeActualCount = unsafeActualCount;
    }

    public int getSynchronizedActualCount() {
        return synchronizedActualCount;
    }

    public void setSynchronizedActualCount(int synchronizedActualCount) {
        this.synchronizedActualCount = synchronizedActualCount;
    }

    public int getAtomicActualCount() {
        return atomicActualCount;
    }

    public void setAtomicActualCount(int atomicActualCount) {
        this.atomicActualCount = atomicActualCount;
    }
}
