package ru.vladislav.bekrenev.ticketprocessorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class ProcessedTicketView {

    private UUID id;

    private String title;

    private String description;

    private String category;

    private String status;

    private LocalDateTime createdAt;

    private String priority;

    private Byte slaHours;

    private LocalDateTime processedAt;
}
