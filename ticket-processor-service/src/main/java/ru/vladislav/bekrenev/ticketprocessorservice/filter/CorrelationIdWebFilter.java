package ru.vladislav.bekrenev.ticketprocessorservice.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Order(1)
@Slf4j
public class CorrelationIdWebFilter implements WebFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String CORRELATION_ID_KEY = "correlationId";

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/actuator/health",
            "/actuator/prometheus",
            "/actuator/metrics",
            "/actuator/info",
            "/actuator"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (EXCLUDED_PATHS.stream().anyMatch(path::startsWith)) {
            log.debug("Skipping correlation ID check for path: {}", path);
            return chain.filter(exchange);
        }

        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            String errorMessage = "{\"error\": \"Missing required header: X-Correlation-Id\"}";
            return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse().bufferFactory()
                            .wrap(errorMessage.getBytes()))
            );
        }

        exchange.getResponse().getHeaders().add(CORRELATION_ID_HEADER, correlationId);

        ThreadContext.put(CORRELATION_ID_KEY, correlationId);

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    HttpStatus status = (HttpStatus) exchange.getResponse().getStatusCode();
                    if (status != null) {
                        log.info("Request completed with status: {}", status.value());
                    }
                    ThreadContext.clearAll();
                });
    }
}