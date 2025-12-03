package ru.vladislav.bekrenev.ticketprocessorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCreatedEventDTO {

    private String eventId;
    private String correlationId;
    private String eventType;
    private String timestamp;
    private TicketPayload payload;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketPayload {
        private String id;
        private String title;
        private String description;
        private String category;
        private String status;
        private String createdAt;
    }
}