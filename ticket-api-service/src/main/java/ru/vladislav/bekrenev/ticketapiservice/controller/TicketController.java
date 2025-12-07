package ru.vladislav.bekrenev.ticketapiservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vladislav.bekrenev.ticketapiservice.dto.TicketCreateDTO;
import ru.vladislav.bekrenev.ticketapiservice.entity.Ticket;
import ru.vladislav.bekrenev.ticketapiservice.entity.TicketStatus;
import ru.vladislav.bekrenev.ticketapiservice.service.TicketService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/create")
    public Mono<ResponseEntity<Ticket>> createTicket(
            @Valid @RequestBody TicketCreateDTO ticket) {

        String correlationId = MDC.get("correlationId");

        return ticketService.createTicket(ticket, correlationId)
                .map(createdTicket -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .header("X-Correlation-Id", correlationId )
                        .body(createdTicket)
                );
    }


    @GetMapping("/{id}")
    public Mono<ResponseEntity<Ticket>> getTicket(@PathVariable UUID id) {
        return null;
    }

    @GetMapping
    public Flux<ResponseEntity<Ticket>> getAllTickets(@RequestParam TicketStatus status,
                                      @RequestParam int page,
                                      @RequestParam int size) {
        return null;
    }

}
