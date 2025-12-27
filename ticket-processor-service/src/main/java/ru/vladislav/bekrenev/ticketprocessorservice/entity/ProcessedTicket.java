package ru.vladislav.bekrenev.ticketprocessorservice.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "processed_tickets")
@Builder
@Data
public class ProcessedTicket implements Persistable<UUID> {

    @Id
    private UUID id;

    private String title;

    private String description;

    private String category;

    private String status;

    private LocalDateTime createdAt;

    private String priority;

    private Byte slaHours;

    private LocalDateTime processedAt;

    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }
}
