package ru.vladislav.bekrenev.ticketapiservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import ru.vladislav.bekrenev.ticketapiservice.annotation.LogExecution;
import ru.vladislav.bekrenev.ticketapiservice.dto.TicketCreateDTO;
import ru.vladislav.bekrenev.ticketapiservice.dto.TicketResponseDTO;
import ru.vladislav.bekrenev.ticketapiservice.dto.kafkaevent.TicketCreatedEventDTO;
import ru.vladislav.bekrenev.ticketapiservice.dto.kafkaevent.TicketPayload;
import ru.vladislav.bekrenev.ticketapiservice.entity.Ticket;
import ru.vladislav.bekrenev.ticketapiservice.exeption.KafkaPublishException;
import ru.vladislav.bekrenev.ticketapiservice.exeption.TicketNotFoundException;
import ru.vladislav.bekrenev.ticketapiservice.filter.CorrelationIdWebFilter;
import ru.vladislav.bekrenev.ticketapiservice.publisher.TicketEventPublisher;
import ru.vladislav.bekrenev.ticketapiservice.repository.TicketRepository;
import ru.vladislav.bekrenev.ticketapiservice.util.mapper.ticketmapper.TicketMapper;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static reactor.core.publisher.Mono.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketEventPublisher ticketEventPublisher;

    @LogExecution
    public Mono<TicketResponseDTO> createTicket(TicketCreateDTO ticketCreateDTO,
                                                String correlationId) {

        return just(ticketCreateDTO)
                .map(TicketMapper::toEntity)
                .flatMap(ticketRepository::save)

                .flatMap(savedTicket ->
                        publishToKafka(savedTicket, correlationId)
                                .thenReturn(savedTicket)
                )
                .flatMap(savedTicket -> {
                    return Mono.deferContextual(ctx -> {
                        Map<String, String> oldContext = ctx.getOrDefault(
                                CorrelationIdWebFilter.MDC_CONTEXT_KEY,
                                new HashMap<>()
                        );
                        Map<String, String> newContext = new HashMap<>(oldContext);
                        newContext.put("ticketId", savedTicket.getId().toString());
                        newContext.put("category", savedTicket.getCategory().name());

                        TicketResponseDTO response = TicketMapper.toResponse(savedTicket);
                        return just(response)
                                .contextWrite(Context.of(
                                        CorrelationIdWebFilter.MDC_CONTEXT_KEY,
                                        newContext
                                ));
                    });
                });
    }

    @LogExecution
    public Mono<TicketResponseDTO> getTicket(UUID id) {
        ThreadContext.put("ticketId", id.toString());
        return ticketRepository.findById(id)
                .map(TicketMapper::toResponse)
                .switchIfEmpty(error(new TicketNotFoundException(id)));
    }


    private Mono<Void> publishToKafka(Ticket ticket, String correlationId) {
        return defer(() -> {
            TicketPayload payload = TicketMapper.toPayload(ticket);

            TicketCreatedEventDTO<TicketPayload> event = TicketCreatedEventDTO.<TicketPayload>builder()
                    .eventId(UUID.randomUUID().toString())
                    .correlationId(correlationId)
                    .eventType("TICKET_CREATED")
                    .timestamp(Instant.now().toString())
                    .payload(payload)
                    .build();

            try {
                ticketEventPublisher.publishTicketCreated(event);
                return empty();
            } catch (Exception e) {
                throw new KafkaPublishException(
                        "Failed to publish to Kafka: " + e.getMessage(), e);
            }
        });
    }
}