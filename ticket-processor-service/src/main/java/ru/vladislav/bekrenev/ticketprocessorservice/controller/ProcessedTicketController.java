package ru.vladislav.bekrenev.ticketprocessorservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.vladislav.bekrenev.ticketprocessorservice.dto.ProcessedTicketResponseDTO;
import ru.vladislav.bekrenev.ticketprocessorservice.dto.StatsSummaryDTO;
import ru.vladislav.bekrenev.ticketprocessorservice.service.impl.ProcessedTicketServiceImpl;
import ru.vladislav.bekrenev.ticketprocessorservice.util.mapper.TicketMapper;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProcessedTicketController {

    private final ProcessedTicketServiceImpl ticketService;
    private final TicketMapper responseMapper;

    @GetMapping("/stats/summary")
    public Mono<ResponseEntity<StatsSummaryDTO>> getStatsSummary(
            @RequestHeader("X-Correlation-Id") String correlationId
    ) {
        ThreadContext.put("correlationId", correlationId);
        return ticketService.getStatsSummary()
                .map(stats -> {
                    log.info("Stats summary retrieved successfully");
                    return ResponseEntity
                            .ok()
                            .header("X-Correlation-Id", correlationId)
                            .body(stats);
                })
                .doOnError(err -> {
                    log.error("Error in getStatsSummary: {}", err.getMessage());
                })
                .doFinally(signal -> {
                    ThreadContext.clearAll();
                });
    }

    @GetMapping("/tickets/processed")
    public Mono<ResponseEntity<Page<ProcessedTicketResponseDTO>>> getProcessedTickets(
            @RequestParam(required = false) String status,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestHeader("X-Correlation-Id") String correlationId
    ) {
        ThreadContext.put("correlationId", correlationId);
        Pageable pageable = PageRequest.of(page, size);
        return ticketService.getProcessedTickets(status, pageable)
                .map(ticketPage -> ticketPage.map(responseMapper::toDto))
                .map(tickets -> {
                    log.info("Processed tickets retrieved successfully");
                    return ResponseEntity
                            .ok()
                            .header("X-Correlation-Id", correlationId)
                            .body(tickets);
                })
                .doOnError(err -> {
                    log.error("Error in getProcessedTickets: {}", err.getMessage());
                })
                .doFinally(signal -> {
                    ThreadContext.clearAll();
                });
    }
}