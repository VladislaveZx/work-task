package ru.vladislav.bekrenev.ticketapiservice.util.mapper.ticketmapper;

import org.springframework.stereotype.Component;
import ru.vladislav.bekrenev.ticketapiservice.dto.TicketCreateDTO;
import ru.vladislav.bekrenev.ticketapiservice.dto.kafkaevent.TicketCreatedEventDTO;
import ru.vladislav.bekrenev.ticketapiservice.dto.kafkaevent.TicketPayload;
import ru.vladislav.bekrenev.ticketapiservice.entity.Ticket;
import ru.vladislav.bekrenev.ticketapiservice.entity.TicketCategory;
import ru.vladislav.bekrenev.ticketapiservice.entity.TicketStatus;

import java.time.LocalDateTime;

@Component
public class TicketMapper {
    
    public static Ticket toEntity(TicketCreateDTO ticketCreateDTO) {
        return Ticket.builder()
                .title(ticketCreateDTO.getTitle())
                .description(ticketCreateDTO.getDescription())
                .createdAt(LocalDateTime.now())
                .status(TicketStatus.NEW)
                .category(TicketCategory.valueOf(ticketCreateDTO.getCategory().toUpperCase()))
                .build();
    }

    public static TicketPayload toPayload(Ticket ticket) {
        return new TicketPayload(
                ticket.getId().toString(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getCategory().name(),
                ticket.getStatus().name(),
                ticket.getCreatedAt().toString()
        );
    }

}

