package ru.vladislav.bekrenev.ticketapiservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.vladislav.bekrenev.ticketapiservice.entity.Ticket;
import ru.vladislav.bekrenev.ticketapiservice.service.TicketService;

@RestController
@RequestMapping("/api/v1/tickets/")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/create")
    public Ticket createTicket(@RequestBody Ticket ticket) {
        return null;
    }

    @GetMapping("/{id}")
    public Ticket getTicket(@PathVariable Long id) {
        return null;
    }
}

