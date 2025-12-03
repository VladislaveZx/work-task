package ru.vladislav.bekrenev.ticketprocessorservice.util.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import ru.vladislav.bekrenev.ticketprocessorservice.dto.TicketCreatedEventDTO;
import ru.vladislav.bekrenev.ticketprocessorservice.entity.Category;
import ru.vladislav.bekrenev.ticketprocessorservice.entity.ProcessedTicket;
import ru.vladislav.bekrenev.ticketprocessorservice.entity.Priority;
import ru.vladislav.bekrenev.ticketprocessorservice.entity.Status;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class TicketMapper {

    public ProcessedTicket mapToProcessedTicket(TicketCreatedEventDTO.TicketPayload payload) {
        return ProcessedTicket.builder()
                .id(UUID.fromString(payload.getId()))
                .title(payload.getTitle())
                .description(payload.getDescription())
                .category(Category.valueOf(payload.getCategory()))
                .status(Status.valueOf(payload.getStatus()))
                .createdAt(LocalDateTime.parse(payload.getCreatedAt()))
                .priority(calculatePriority(payload.getCategory()))
                .slaHours(calculateSla(payload.getCategory()))
                .processedAt(LocalDateTime.now())
                .build();
    }

    private Priority calculatePriority(String category) {
        return switch (category) {
            case "TECH" -> Priority.HIGH;
            case "PAYMENT" -> Priority.CRITICAL;
            default -> Priority.LOW;
        };
    }

    private byte calculateSla(String category) {
        return switch (category) {
            case "TECH" -> 24;
            case "PAYMENT" -> 4;
            default -> 48;
        };
    }
}