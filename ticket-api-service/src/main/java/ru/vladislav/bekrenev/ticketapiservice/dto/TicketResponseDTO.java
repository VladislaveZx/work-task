package ru.vladislav.bekrenev.ticketapiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.vladislav.bekrenev.ticketapiservice.entity.TicketCategory;
import ru.vladislav.bekrenev.ticketapiservice.entity.TicketStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponseDTO {

    private String title;

    private String description;

    private String category;

    private String status;
}
