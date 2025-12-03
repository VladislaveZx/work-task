package ru.vladislav.bekrenev.ticketapiservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Table("tickets")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    private UUID id;

    private String topic;

    private String description;

    private TicketStatus status;

    @Column("created_at")
    private LocalDateTime createdAt;
}
