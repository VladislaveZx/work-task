package ru.vladislav.bekrenev.ticketapiservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.vladislav.bekrenev.ticketapiservice.annotation.LogExecution;
import ru.vladislav.bekrenev.ticketapiservice.dto.TicketCreateDTO;
import ru.vladislav.bekrenev.ticketapiservice.dto.TicketResponseDTO;
import ru.vladislav.bekrenev.ticketapiservice.dto.kafkaevent.TicketCreatedEventDTO;
import ru.vladislav.bekrenev.ticketapiservice.dto.kafkaevent.TicketPayload;
import ru.vladislav.bekrenev.ticketapiservice.entity.Ticket;
import ru.vladislav.bekrenev.ticketapiservice.publisher.TicketEventPublisher;
import ru.vladislav.bekrenev.ticketapiservice.repository.TicketRepository;
import ru.vladislav.bekrenev.ticketapiservice.util.mapper.ticketmapper.TicketMapper;

import java.time.Instant;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketEventPublisher ticketEventPublisher;

    @LogExecution
//    @Transactional
    public Mono<TicketResponseDTO> createTicket(TicketCreateDTO ticketCreateDTO, String correlationId) {
        return Mono.just(ticketCreateDTO)
                .map(TicketMapper::toEntity)
                .flatMap(ticketRepository::save)
                .doOnError(Throwable::printStackTrace)
                .flatMap(ticket -> {
                    TicketPayload payload = TicketMapper.toPayload(ticket);

                    TicketCreatedEventDTO<TicketPayload> event = TicketCreatedEventDTO.<TicketPayload>builder()
                            .eventId(UUID.randomUUID().toString())
                            .correlationId(correlationId)
                            .eventType("TICKET_CREATED")
                            .timestamp(Instant.now().toString())
                            .payload(payload)
                            .build();

                    return ticketEventPublisher.publishTicketCreated(event)
                            .thenReturn(ticket)
                            .onErrorResume(e -> ticketRepository.deleteById(ticket.getId())
                                    .then(Mono.error(new RuntimeException(
                                            String.format("Failed to send ticket to Kafka. Ticket ID: %s was deleted. Correlation ID: %s",
                                                    ticket.getId(), correlationId), e))));
                })
                .map(TicketMapper::ToResponse)
                .doOnError(Throwable::printStackTrace);
    }

    public Mono<TicketResponseDTO> getTicket(UUID id) {
        return ticketRepository.findById(id)
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("Ticket not found: {}", id);
                    return Mono.empty();
                }))
                .map(TicketMapper::ToResponse)
                .doOnError(error ->
                        log.error("Database error retrieving ticket {}: {}", id, error.getMessage()));
    }


}
