package ru.vladislav.bekrenev.ticketapiservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vladislav.bekrenev.ticketapiservice.dto.TicketCreateDTO;
import ru.vladislav.bekrenev.ticketapiservice.dto.TicketResponseDTO;
import ru.vladislav.bekrenev.ticketapiservice.entity.Ticket;
import ru.vladislav.bekrenev.ticketapiservice.entity.TicketStatus;
import ru.vladislav.bekrenev.ticketapiservice.service.TicketService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Slf4j
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public Mono<ResponseEntity<TicketResponseDTO>> createTicket(
            @Valid @RequestBody TicketCreateDTO ticket,
            @RequestHeader("X-Correlation-Id") String correlationId
    ) {

        return ticketService.createTicket(ticket, correlationId)
                .map(createdTicket -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .header("X-Correlation-Id", correlationId )
                        .body(createdTicket)
                );
    }


    @GetMapping("/{id}")
    public Mono<ResponseEntity<TicketResponseDTO>> getTicket(@PathVariable UUID id,
                                                             @RequestHeader("X-Correlation-Id") String correlationId
                                                  ) {
        return ticketService.getTicket(id)
                .map(ticket -> ResponseEntity
                        .status(HttpStatus.OK)
                        .header("X-Correlation-Id", correlationId)
                        .body(ticket)
                );
    }

    @GetMapping
    public Flux<ResponseEntity<TicketResponseDTO>> getAllTickets(@RequestParam TicketStatus status,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestHeader("X-Correlation-Id") String correlationId
                                                      ) {
        return null;
    }

}
