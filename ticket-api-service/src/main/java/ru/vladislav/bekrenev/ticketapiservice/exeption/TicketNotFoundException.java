package ru.vladislav.bekrenev.ticketapiservice.exeption;

import java.util.UUID;

public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(UUID id) {
        super(String.format("Ticket with id %s not found", id));
    }

    public TicketNotFoundException(String message) {
        super(message);
    }
}