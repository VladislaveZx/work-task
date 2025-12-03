package ru.vladislav.bekrenev.ticketprocessorservice.entity;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "processed_tickets")
@Builder
public class ProcessedTicket {

    @Id
    private UUID id;

    private String title;

    private String description;

    private Category category;

    private Status status;

    private LocalDateTime createdAt;

    private Priority priority;

    private Byte slaHours;

    private LocalDateTime processedAt;
}
