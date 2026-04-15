package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class ThreadSafeMetricsService {
    private final AtomicLong taskIdGenerator = new AtomicLong(1L);

    public long nextTaskId() {
        return taskIdGenerator.getAndIncrement();
    }
}
