package ru.vladislav.bekrenev.ticketapiservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.vladislav.bekrenev.ticketapiservice.annotation.LogExecution;
import ru.vladislav.bekrenev.ticketapiservice.dto.TicketCreateDTO;
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
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketEventPublisher ticketEventPublisher;

    @LogExecution
    public Mono<Ticket> createTicket(TicketCreateDTO ticketCreateDTO, String correlationId) {
        return Mono.just(ticketCreateDTO)
                .map(TicketMapper::toEntity)
                .flatMap(ticketRepository::save)
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
                            .thenReturn(ticket);
                });
    }
}
