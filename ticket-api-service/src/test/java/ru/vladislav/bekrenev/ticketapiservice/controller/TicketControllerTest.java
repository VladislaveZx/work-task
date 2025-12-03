package ru.vladislav.bekrenev.ticketapiservice.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.vladislav.bekrenev.ticketapiservice.dto.TicketCreateDTO;
import ru.vladislav.bekrenev.ticketapiservice.dto.TicketResponseDTO;
import ru.vladislav.bekrenev.ticketapiservice.entity.TicketStatus;
import ru.vladislav.bekrenev.ticketapiservice.service.impl.TicketServiceImpl;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    @Mock
    private TicketServiceImpl ticketService;

    private final String CORRELATION_ID = "test-correlation-id";

    @Test
    void createTicket_ShouldReturnCreatedResponse() {

        TicketController controller = new TicketController(ticketService);

        TicketCreateDTO createDTO = TicketCreateDTO.builder()
                .title("Test Ticket")
                .description("Test Description")
                .category("TECH")
                .build();

        TicketResponseDTO responseDTO = TicketResponseDTO.builder()
                .title("Test Ticket")
                .description("Test Description")
                .category("TECH")
                .status(TicketStatus.NEW.name())
                .build();

        when(ticketService.createTicket(any(TicketCreateDTO.class), eq(CORRELATION_ID)))
                .thenReturn(Mono.just(responseDTO));

        StepVerifier.create(controller.createTicket(createDTO, CORRELATION_ID))
                .expectNextMatches(responseEntity ->
                        responseEntity.getStatusCode() == HttpStatus.CREATED &&
                                responseEntity.getHeaders().getFirst("X-Correlation-Id").equals(CORRELATION_ID) &&
                                responseEntity.getBody().getTitle().equals("Test Ticket")
                )
                .verifyComplete();

        verify(ticketService, times(1)).createTicket(createDTO, CORRELATION_ID);
    }

    @Test
    void getTicket_ShouldReturnOkResponse() {

        TicketController controller = new TicketController(ticketService);

        UUID ticketId = UUID.randomUUID();
        TicketResponseDTO responseDTO = TicketResponseDTO.builder()
                .title("Test Ticket")
                .description("Test Description")
                .category("TECH")
                .status(TicketStatus.NEW.name())
                .build();

        when(ticketService.getTicket(ticketId))
                .thenReturn(Mono.just(responseDTO));

        StepVerifier.create(controller.getTicket(ticketId, CORRELATION_ID))
                .expectNextMatches(responseEntity ->
                        responseEntity.getStatusCode() == HttpStatus.OK &&
                                responseEntity.getHeaders().getFirst("X-Correlation-Id").equals(CORRELATION_ID) &&
                                responseEntity.getBody().getTitle().equals("Test Ticket")
                )
                .verifyComplete();

        verify(ticketService, times(1)).getTicket(ticketId);
    }

    @Test
    void getTicket_WhenTicketNotFound_ShouldReturnEmptyMono() {

        TicketController controller = new TicketController(ticketService);

        UUID ticketId = UUID.randomUUID();
        when(ticketService.getTicket(ticketId))
                .thenReturn(Mono.empty());

        StepVerifier.create(controller.getTicket(ticketId, CORRELATION_ID))
                .verifyComplete();

        verify(ticketService, times(1)).getTicket(ticketId);
    }

    @Test
    void getAllTickets_ShouldReturnFluxOfTickets() {

        TicketController controller = new TicketController(ticketService);

        TicketResponseDTO responseDTO = TicketResponseDTO.builder()
                .title("Test Ticket")
                .description("Test Description")
                .category("TECH")
                .status(TicketStatus.NEW.name())
                .build();

        when(ticketService.findAllPaged(0, 20))
                .thenReturn(Flux.just(responseDTO));

        StepVerifier.create(controller.getAllTickets(0, 20, CORRELATION_ID))
                .expectNextMatches(responseEntity -> {
                    if (responseEntity.getStatusCode() != HttpStatus.OK) return false;
                    if (!responseEntity.getHeaders().getFirst("X-Correlation-Id").equals(CORRELATION_ID)) return false;

                    StepVerifier.create(responseEntity.getBody())
                            .expectNextMatches(ticket ->
                                    ticket.getTitle().equals("Test Ticket")
                            )
                            .verifyComplete();
                    return true;
                })
                .verifyComplete();

        verify(ticketService, times(1)).findAllPaged(0, 20);
    }

    @Test
    void getTicketsByStatus_ShouldReturnFluxOfTickets() {
        // Arrange
        TicketController controller = new TicketController(ticketService);

        TicketResponseDTO responseDTO = TicketResponseDTO.builder()
                .title("Test Ticket")
                .description("Test Description")
                .category("TECH")
                .status(TicketStatus.NEW.name())
                .build();

        when(ticketService.findByStatusPaged(TicketStatus.NEW, 0, 20))
                .thenReturn(Flux.just(responseDTO));

        // Act & Assert
        StepVerifier.create(controller.getTicketsByStatus(TicketStatus.NEW, 0, 20, CORRELATION_ID))
                .expectNextMatches(responseEntity -> {
                    if (responseEntity.getStatusCode() != HttpStatus.OK) return false;
                    if (!responseEntity.getHeaders().getFirst("X-Correlation-Id").equals(CORRELATION_ID)) return false;

                    StepVerifier.create(responseEntity.getBody())
                            .expectNextMatches(ticket ->
                                    ticket.getStatus().equals(TicketStatus.NEW.name())
                            )
                            .verifyComplete();
                    return true;
                })
                .verifyComplete();

        verify(ticketService, times(1)).findByStatusPaged(TicketStatus.NEW, 0, 20);
    }
}