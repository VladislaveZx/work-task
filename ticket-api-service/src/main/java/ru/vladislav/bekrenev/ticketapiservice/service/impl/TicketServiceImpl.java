package ru.vladislav.bekrenev.ticketapiservice.service.impl;

import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vladislav.bekrenev.ticketapiservice.annotation.LogExecution;
import ru.vladislav.bekrenev.ticketapiservice.config.MetricConfig;
import ru.vladislav.bekrenev.ticketapiservice.dto.TicketCreateDTO;
import ru.vladislav.bekrenev.ticketapiservice.dto.TicketResponseDTO;
import ru.vladislav.bekrenev.ticketapiservice.dto.kafkaevent.TicketCreatedEventDTO;
import ru.vladislav.bekrenev.ticketapiservice.dto.kafkaevent.TicketPayload;
import ru.vladislav.bekrenev.ticketapiservice.entity.Ticket;
import ru.vladislav.bekrenev.ticketapiservice.entity.TicketStatus;
import ru.vladislav.bekrenev.ticketapiservice.exeption.KafkaPublishException;
import ru.vladislav.bekrenev.ticketapiservice.exeption.TicketNotFoundException;
import ru.vladislav.bekrenev.ticketapiservice.publisher.TicketEventPublisher;
import ru.vladislav.bekrenev.ticketapiservice.repository.TicketRepository;
import ru.vladislav.bekrenev.ticketapiservice.service.TicketService;
import ru.vladislav.bekrenev.ticketapiservice.util.mapper.ticketmapper.TicketMapper;

import java.time.Instant;
import java.util.UUID;

import static reactor.core.publisher.Mono.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TicketEventPublisher ticketEventPublisher;
    private final MetricConfig metricConfig;

    @LogExecution
    public Mono<TicketResponseDTO> createTicket(TicketCreateDTO ticketCreateDTO,
                                                String correlationId) {
        Timer.Sample requestTimer = metricConfig.startRequestLatencyTimer();
        return Mono.just(ticketCreateDTO)
                .map(TicketMapper::toEntity)
                .flatMap(ticket -> {
                    Timer.Sample dbTimer = metricConfig.startDatabaseSaveTimer();
                    return ticketRepository.save(ticket)
                            .doOnSuccess(saved -> {
                                metricConfig.recordDatabaseSaveTime(dbTimer);
                                metricConfig.incrementTicketsCreated();
                                metricConfig.incrementNewTickets();
                            })
                            .doOnError(e -> metricConfig.incrementDatabaseErrors());
                })
                .flatMap(savedTicket ->
                        publishToKafka(savedTicket, correlationId)
                                .doOnSuccess(__ -> metricConfig.incrementKafkaMessagesSent())
                                .doOnError(error -> metricConfig.incrementKafkaError())
                                .thenReturn(savedTicket)
                )
                .map(TicketMapper::toResponse)
                .doOnSuccess(response ->
                        metricConfig.recordRequestLatency(requestTimer)
                )
                .doOnError(error ->
                        metricConfig.recordRequestLatency(requestTimer)
                );
    }

    @LogExecution
    public Mono<TicketResponseDTO> getTicket(UUID id) {
        ThreadContext.put("ticketId", id.toString());
        Timer.Sample requestTimer = metricConfig.startRequestLatencyTimer();
        return ticketRepository.findById(id)
                .doOnError(error -> metricConfig.incrementDatabaseErrors())
                .map(TicketMapper::toResponse)
                .doOnSuccess(response -> metricConfig.recordRequestLatency(requestTimer))
                .doOnError(error -> metricConfig.incrementDatabaseErrors())
                .switchIfEmpty(error(new TicketNotFoundException(id)));
    }

    public Flux<TicketResponseDTO> findByStatusPaged(TicketStatus status, int page, int size) {
        Timer.Sample timer = metricConfig.startRequestLatencyTimer();

        return ticketRepository.findByStatusPaged(status, page, size)
                .map(TicketMapper::toResponse)

                .doOnComplete(() ->
                        metricConfig.recordRequestLatency(timer))
                .doOnError(error -> {
                    metricConfig.recordRequestLatency(timer);
                    metricConfig.incrementKafkaError();
                });
    }

    public Flux<TicketResponseDTO> findAllPaged(int page, int size) {
        Timer.Sample timer = metricConfig.startRequestLatencyTimer();

        return ticketRepository.findAllPaged(page, size)
                .map(TicketMapper::toResponse)
                .doOnComplete(() ->
                        metricConfig.recordRequestLatency(timer))
                .doOnError(error -> {
                    metricConfig.recordRequestLatency(timer);
                    metricConfig.incrementKafkaError();
                });
    }


    private Mono<Void> publishToKafka(Ticket ticket, String correlationId) {
        TicketPayload payload = TicketMapper.toPayload(ticket);

        TicketCreatedEventDTO<TicketPayload> event = TicketCreatedEventDTO.<TicketPayload>builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .eventType("TICKET_CREATED")
                .timestamp(Instant.now().toString())
                .payload(payload)
                .build();

        return Mono.just(event)
                .flatMap(e -> ticketEventPublisher.publishTicketCreated(e)
                        .doOnSuccess(publishedEvent -> {
                        }))
                .onErrorMap(e -> new KafkaPublishException(
                        "Failed to publish to Kafka: " + e.getMessage(), e));
    }
}