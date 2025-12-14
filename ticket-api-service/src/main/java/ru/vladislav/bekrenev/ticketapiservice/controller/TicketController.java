package ru.vladislav.bekrenev.ticketapiservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vladislav.bekrenev.ticketapiservice.dto.TicketCreateDTO;
import ru.vladislav.bekrenev.ticketapiservice.dto.TicketResponseDTO;
import ru.vladislav.bekrenev.ticketapiservice.entity.TicketStatus;
import ru.vladislav.bekrenev.ticketapiservice.service.impl.TicketServiceImpl;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Slf4j
public class TicketController {

    private final TicketServiceImpl ticketService;

    @PostMapping
    public Mono<ResponseEntity<TicketResponseDTO>> createTicket(
            @Valid @RequestBody TicketCreateDTO ticket,
            @RequestHeader("X-Correlation-Id") String correlationId
    ) {
        ThreadContext.put("correlationId", correlationId);
        log.info("Creating ticket request");

        return ticketService.createTicket(ticket, correlationId)
                .map(createdTicket -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .header("X-Correlation-Id", correlationId)
                        .body(createdTicket)
                )
                .doFinally(signal -> {
                    ThreadContext.remove("correlationId");
                });
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<TicketResponseDTO>> getTicket(
            @PathVariable UUID id,
            @RequestHeader("X-Correlation-Id") String correlationId
    ) {
        ThreadContext.put("correlationId", correlationId);

        log.info("Getting ticket request");

        return ticketService.getTicket(id)
                .map(ticket -> ResponseEntity
                        .ok()
                        .header("X-Correlation-Id", correlationId)
                        .body(ticket)
                )
                .doFinally(signal -> {
                    ThreadContext.remove("correlationId");
                });
    }

    @GetMapping("/status/{status}")
    public Mono<ResponseEntity<Flux<TicketResponseDTO>>> getTicketsByStatus(
            @PathVariable TicketStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("X-Correlation-Id") String correlationId
    ) {
        ThreadContext.put("correlationId", correlationId);

        Flux<TicketResponseDTO> tickets = ticketService.findByStatusPaged(status, page, size);

        return Mono.just(ResponseEntity
                        .ok()
                        .header("X-Correlation-Id", correlationId)
                        .body(tickets))
                .doFinally(signal ->
                    ThreadContext.remove("correlationId"));
    }

    @GetMapping
    public Mono<ResponseEntity<Flux<TicketResponseDTO>>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("X-Correlation-Id") String correlationId
    ) {
        ThreadContext.put("correlationId", correlationId);

        Flux<TicketResponseDTO> tickets = ticketService.findAllPaged(page, size);

        return Mono.just(ResponseEntity
                        .ok()
                        .header("X-Correlation-Id", correlationId)
                        .body(tickets))
                .doFinally(signal ->
                        ThreadContext.remove("correlationId"));
    }

}