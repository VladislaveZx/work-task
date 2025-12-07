package ru.vladislav.bekrenev.ticketapiservice.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import org.slf4j.MDC;

@Component
public class CorrelationIdWebFilter implements WebFilter {

    private static final String CORRELATION_ID = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID);
        if (correlationId == null || correlationId.isBlank()) {
            return Mono.error(new IllegalArgumentException("Missing X-Correlation-Id header"));
        }

        MDC.put("correlationId", correlationId);

        return chain.filter(exchange)
                .contextWrite(ctx -> ctx.put("correlationId", correlationId))
                .doFinally(signal -> MDC.remove("correlationId"));
    }
}
