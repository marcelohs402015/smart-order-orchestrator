package com.marcelo.orchestrator.infrastructure.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("@annotation(loggable) || @within(loggable)")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint, Loggable loggable) throws Throwable {
        if (loggable == null) {
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getMethod().getName();
        Object[] args = joinPoint.getArgs();

        Instant startTime = loggable.logExecutionTime() ? Instant.now() : null;

        if (loggable.logArgs() && shouldLog(loggable.level(), Loggable.LogLevel.DEBUG)) {
            logAtLevel(loggable.level(), "Executing {}.{} with args: {}", className, methodName, args);
        } else if (shouldLog(loggable.level(), Loggable.LogLevel.INFO)) {
            logAtLevel(loggable.level(), "Executing {}.{}", className, methodName);
        }

        try {
            Object result = joinPoint.proceed();

            if (loggable.logResult() && shouldLog(loggable.level(), Loggable.LogLevel.DEBUG)) {
                logAtLevel(loggable.level(), "Method {}.{} returned: {}", className, methodName, result);
            }

            if (loggable.logExecutionTime() && startTime != null) {
                Duration duration = Duration.between(startTime, Instant.now());
                logAtLevel(loggable.level(), "Method {}.{} executed in {}ms", className, methodName, duration.toMillis());
            }

            return result;
        } catch (Throwable e) {
            log.error("Error executing {}.{}: {}", className, methodName, e.getMessage(), e);
            throw e;
        }
    }

    private boolean shouldLog(Loggable.LogLevel annotationLevel, Loggable.LogLevel requiredLevel) {
        return annotationLevel.ordinal() <= requiredLevel.ordinal();
    }

    private void logAtLevel(Loggable.LogLevel level, String message, Object... args) {
        switch (level) {
            case DEBUG -> log.debug(message, args);
            case INFO -> log.info(message, args);
            case WARN -> log.warn(message, args);
            case ERROR -> log.error(message, args);
        }
    }
}

