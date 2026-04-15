package com.example.demo;

import com.example.demo.service.ThreadSafeMetricsService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThreadSafeMetricsServiceTest {
    @Test
    void atomicCountersAdvanceSequentially() {
        ThreadSafeMetricsService threadSafeMetricsService = new ThreadSafeMetricsService();

        assertEquals(1L, threadSafeMetricsService.nextTaskId());
        assertEquals(2L, threadSafeMetricsService.nextTaskId());
    }
}
