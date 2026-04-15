package com.example.demo;

import com.example.demo.dto.RaceConditionDemoResultDto;
import com.example.demo.service.RaceConditionDemoService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RaceConditionDemoServiceTest {
    private final RaceConditionDemoService raceConditionDemoService = new RaceConditionDemoService();

    @Test
    void runDemoReturnsConsistentSynchronizedCounter() {
        RaceConditionDemoResultDto resultDto = raceConditionDemoService.runDemo(64, 5000);

        assertEquals(320000, resultDto.getExpectedCount());
        assertEquals(resultDto.getExpectedCount(), resultDto.getSynchronizedActualCount());
        assertEquals(resultDto.getExpectedCount(), resultDto.getAtomicActualCount());
        assertTrue(resultDto.getUnsafeActualCount() <= resultDto.getExpectedCount());
    }

    @Test
    void runDemoCanFinishWithoutRaceConditionForSingleThread() {
        RaceConditionDemoResultDto resultDto = raceConditionDemoService.runDemo(1, 1);

        assertEquals(1, resultDto.getExpectedCount());
        assertEquals(1, resultDto.getUnsafeActualCount());
        assertEquals(1, resultDto.getSynchronizedActualCount());
        assertEquals(1, resultDto.getAtomicActualCount());
    }

    @Test
    void runDemoThrowsWhenCurrentThreadInterrupted() {
        Thread.currentThread().interrupt();

        try {
            assertThrows(IllegalStateException.class, () -> raceConditionDemoService.runDemo(64, 5000));
        } finally {
            Thread.interrupted();
        }
    }
}
