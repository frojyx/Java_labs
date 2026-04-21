package com.example.demo.service;

import com.example.demo.dto.RaceConditionDemoResultDto;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RaceConditionDemoService {
    private static final int UNSAFE_YIELD_MASK = 255;

    public RaceConditionDemoResultDto runDemo(int threadCount, int incrementsPerThread) {
        UnsafeCounter unsafeCounter = new UnsafeCounter();
        SynchronizedCounter synchronizedCounter = new SynchronizedCounter();
        AtomicCounter atomicCounter = new AtomicCounter();
        int expectedCount = threadCount * incrementsPerThread;

        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch latch = new CountDownLatch(threadCount);

        try (ExecutorService executorService = Executors.newFixedThreadPool(threadCount)) {
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() ->
                    runWorker(
                        startSignal,
                        latch,
                        incrementsPerThread,
                        unsafeCounter,
                        synchronizedCounter,
                        atomicCounter
                    )
                );
            }

            startSignal.countDown();
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Race condition demo was interrupted", exception);
        }

        RaceConditionDemoResultDto resultDto = new RaceConditionDemoResultDto();
        resultDto.setThreadCount(threadCount);
        resultDto.setIncrementsPerThread(incrementsPerThread);
        resultDto.setExpectedCount(expectedCount);
        resultDto.setUnsafeActualCount(unsafeCounter.getValue());
        resultDto.setSynchronizedActualCount(synchronizedCounter.getValue());
        resultDto.setAtomicActualCount(atomicCounter.getValue());
        return resultDto;
    }

    private void runWorker(CountDownLatch startSignal, CountDownLatch latch, int incrementsPerThread,
                           UnsafeCounter unsafeCounter, SynchronizedCounter synchronizedCounter,
                           AtomicCounter atomicCounter) {
        try {
            startSignal.await();
            for (int step = 0; step < incrementsPerThread; step++) {
                unsafeCounter.increment();
                synchronizedCounter.increment();
                atomicCounter.increment();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Race condition demo worker was interrupted", exception);
        } finally {
            latch.countDown();
        }
    }

    private static final class UnsafeCounter {
        private int value;

        private void increment() {
            int current = value;
            if ((current & UNSAFE_YIELD_MASK) == 0) {
                Thread.yield();
            }
            value = current + 1;
        }

        private int getValue() {
            return value;
        }
    }

    private static final class SynchronizedCounter {
        private int value;

        private synchronized void increment() {
            value++;
        }

        private synchronized int getValue() {
            return value;
        }
    }

    private static final class AtomicCounter {
        private final AtomicInteger value = new AtomicInteger();

        private void increment() {
            value.incrementAndGet();
        }

        private int getValue() {
            return value.get();
        }
    }
}
