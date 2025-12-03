package ru.vladislav.bekrenev.ticketapiservice.config.context;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;

@Configuration
@Slf4j
public class ReactorContextConfig {

    public static final String CONTEXT_KEY = "mdcContext";

    @PostConstruct
    public void contextOperatorHook() {
        Hooks.onEachOperator(CONTEXT_KEY,
                Operators.lift((sc, subscriber) ->
                        new MdcContextLifter<>(subscriber)));
        log.info("MDC context propagation enabled");
    }

    @PreDestroy
    public void cleanupHook() {
        Hooks.resetOnEachOperator(CONTEXT_KEY);
        log.info("MDC context propagation cleaned up");
    }
}