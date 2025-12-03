package ru.vladislav.bekrenev.ticketapiservice.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Aspect
@Component
@Slf4j
public class LoggingAspect {


    @Around("@annotation(ru.vladislav.bekrenev.ticketapiservice.annotation.LogExecution)")
    public Object logExecution(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = pjp.proceed();

        if (result instanceof Mono<?> mono) {
            return mono
                    .doOnSubscribe(s -> log.info("{} started", pjp.getSignature().toShortString()))
                    .doOnSuccess(v -> log.info("{} finished in {} ms",
                            pjp.getSignature().toShortString(),
                            System.currentTimeMillis() - start));
        }

        if (result instanceof Flux<?> flux) {
            return flux
                    .doOnSubscribe(s -> log.info("{} started", pjp.getSignature().toShortString()))
                    .doOnComplete(() -> log.info("{} finished in {} ms",
                            pjp.getSignature().toShortString(),
                            System.currentTimeMillis() - start));
        }

        log.info("{} finished in {} ms", pjp.getSignature().toShortString(),
                System.currentTimeMillis() - start);
        return result;
    }

}