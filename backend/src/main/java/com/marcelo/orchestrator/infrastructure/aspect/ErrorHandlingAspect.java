package com.marcelo.orchestrator.infrastructure.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Aspect
@Component
public class ErrorHandlingAspect {

    @Around("@annotation(errorHandler)")
    public Object handleErrors(ProceedingJoinPoint joinPoint, ErrorHandler errorHandler) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (WebClientResponseException e) {
            log.error("WebClient error in {}: status={}, body={}", 
                joinPoint.getSignature().toShortString(), 
                e.getStatusCode(), 
                e.getResponseBodyAsString());
            
            if (errorHandler.handledExceptions().length > 0) {
                for (Class<? extends RuntimeException> exceptionClass : errorHandler.handledExceptions()) {
                    if (exceptionClass.isInstance(e)) {
                        throw exceptionClass.cast(e);
                    }
                }
            }
            
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in {}: {}", 
                joinPoint.getSignature().toShortString(), 
                e.getMessage(), e);
            throw e;
        }
    }
}

