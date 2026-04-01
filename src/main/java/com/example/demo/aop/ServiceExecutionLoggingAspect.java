package com.example.demo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceExecutionLoggingAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceExecutionLoggingAspect.class);

    @Around("execution(* com.example.demo.service..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;
            String signature = joinPoint.getSignature().toShortString();
            LOGGER.info("Service method {} executed in {} ms", signature, duration);
        }
    }
}
