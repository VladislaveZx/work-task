package ru.vladislav.bekrenev.ticketprocessorservice.util.mapper;

import org.springframework.stereotype.Component;
import ru.vladislav.bekrenev.ticketprocessorservice.dto.ProcessedTicketResponseDTO;
import ru.vladislav.bekrenev.ticketprocessorservice.dto.ProcessedTicketView;
import ru.vladislav.bekrenev.ticketprocessorservice.dto.TicketCreatedEventDTO;
import ru.vladislav.bekrenev.ticketprocessorservice.entity.ProcessedTicket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class TicketMapper {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public ProcessedTicket mapToProcessedTicket(TicketCreatedEventDTO.TicketPayload payload) {
        return ProcessedTicket.builder()
                .id(UUID.fromString(payload.getId()))
                .title(payload.getTitle())
                .description(payload.getDescription())
                .category(payload.getCategory())
                .status(payload.getStatus().toUpperCase())
                .createdAt(parseDateTime(payload.getCreatedAt()))
                .priority(calculatePriority(payload.getCategory()))
                .slaHours(calculateSla(payload.getCategory()))
                .processedAt(LocalDateTime.now())
                .isNew(true)
                .build();
    }

    public ProcessedTicketResponseDTO toDto(ProcessedTicket ticket) {
        return ProcessedTicketResponseDTO.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .category(ticket.getCategory())
                .status(ticket.getStatus())
                .createdAt(ticket.getCreatedAt())
                .priority(ticket.getPriority())
                .slaHours(ticket.getSlaHours())
                .processedAt(ticket.getProcessedAt())
                .build();
    }

    public ProcessedTicketResponseDTO toDto(ProcessedTicketView view) {
        return ProcessedTicketResponseDTO.builder()
                .id(view.getId())
                .title(view.getTitle())
                .description(view.getDescription())
                .category(view.getCategory())
                .status(view.getStatus())
                .createdAt(view.getCreatedAt())
                .priority(view.getPriority())
                .slaHours(view.getSlaHours())
                .processedAt(view.getProcessedAt())
                .build();
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr, FORMATTER);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private String calculatePriority(String category) {
        if (category == null) {
            return "LOW";
        }
        return switch (category) {
            case "TECH" -> "HIGH";
            case "PAYMENT" -> "CRITICAL";
            default -> "LOW";
        };
    }

    private Byte calculateSla(String category) {
        if (category == null) {
            return 48;
        }
        return switch (category) {
            case "TECH" -> 24;
            case "PAYMENT" -> 4;
            default -> 48;
        };
    }
}