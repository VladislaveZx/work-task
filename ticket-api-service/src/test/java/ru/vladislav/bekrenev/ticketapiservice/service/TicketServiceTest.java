package ru.vladislav.bekrenev.ticketapiservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import ru.vladislav.bekrenev.ticketapiservice.dto.TicketCreateDTO;
import ru.vladislav.bekrenev.ticketapiservice.service.impl.TicketServiceImpl;

class TicketServiceTest {

    @InjectMocks
    private TicketServiceImpl ticketService;

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