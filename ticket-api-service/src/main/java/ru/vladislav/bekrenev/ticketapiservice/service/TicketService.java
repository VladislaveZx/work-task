package ru.vladislav.bekrenev.ticketapiservice.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vladislav.bekrenev.ticketapiservice.dto.TicketCreateDTO;
import ru.vladislav.bekrenev.ticketapiservice.dto.TicketResponseDTO;
import ru.vladislav.bekrenev.ticketapiservice.entity.TicketStatus;

import java.util.UUID;

public interface TicketService {

    Mono<TicketResponseDTO> createTicket(TicketCreateDTO ticketCreateDTO, String correlationId);

    Mono<TicketResponseDTO> getTicket(UUID id);

    Flux<TicketResponseDTO> findByStatusPaged(TicketStatus status, int page, int size);

    Flux<TicketResponseDTO> findAllPaged(int page, int size);
}