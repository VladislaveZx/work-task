package ru.vladislav.bekrenev.ticketprocessorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsSummaryDTO {
    private Long totalTickets;
    private Map<String, Long> byStatus;
    private Double avgProcessingTimeSeconds;
}
