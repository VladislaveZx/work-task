package ru.vladislav.bekrenev.ticketapiservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import ru.vladislav.bekrenev.ticketapiservice.dto.TicketCreateDTO;

import static org.junit.jupiter.api.Assertions.*;

class TicketServiceTest {

    @InjectMocks
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTicket() {
        TicketCreateDTO ticketCreateDTO = new TicketCreateDTO();
    }

    @Test
    void getTicket() {
    }
}