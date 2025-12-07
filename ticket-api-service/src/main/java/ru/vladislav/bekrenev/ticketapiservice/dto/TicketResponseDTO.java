package ru.vladislav.bekrenev.ticketapiservice.dto;

import lombok.Builder;
import lombok.Data;
import ru.vladislav.bekrenev.ticketapiservice.entity.TicketCategory;
import ru.vladislav.bekrenev.ticketapiservice.entity.TicketStatus;

@Data
@Builder
public class TicketResponseDTO {

    private String title;

    private String description;

    private String category;

    private String status;
}
