package com.example.demo;

import com.example.demo.dto.RaceConditionDemoResultDto;
import com.example.demo.service.RaceConditionDemoService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

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

    @Test
    void runWorkerThrowsWhenInterruptedWhileWaitingForStartSignal() throws Exception {
        Method runWorker = RaceConditionDemoService.class.getDeclaredMethod(
            "runWorker",
            CountDownLatch.class,
            CountDownLatch.class,
            int.class,
            Class.forName("com.example.demo.service.RaceConditionDemoService$UnsafeCounter"),
            Class.forName("com.example.demo.service.RaceConditionDemoService$SynchronizedCounter"),
            Class.forName("com.example.demo.service.RaceConditionDemoService$AtomicCounter")
        );
        runWorker.setAccessible(true);

        Object unsafeCounter = instantiateCounter("com.example.demo.service.RaceConditionDemoService$UnsafeCounter");
        Object synchronizedCounter =
            instantiateCounter("com.example.demo.service.RaceConditionDemoService$SynchronizedCounter");
        Object atomicCounter = instantiateCounter("com.example.demo.service.RaceConditionDemoService$AtomicCounter");
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch latch = new CountDownLatch(1);

        Thread.currentThread().interrupt();
        try {
            InvocationTargetException exception = assertThrows(
                InvocationTargetException.class,
                () -> runWorker.invoke(
                    raceConditionDemoService,
                    startSignal,
                    latch,
                    1,
                    unsafeCounter,
                    synchronizedCounter,
                    atomicCounter
                )
            );

            assertTrue(exception.getCause() instanceof IllegalStateException);
            assertEquals("Race condition demo worker was interrupted", exception.getCause().getMessage());
            assertEquals(0L, latch.getCount());
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    private Object instantiateCounter(String className) throws Exception {
        Class<?> counterClass = Class.forName(className);
        var constructor = counterClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }
}
