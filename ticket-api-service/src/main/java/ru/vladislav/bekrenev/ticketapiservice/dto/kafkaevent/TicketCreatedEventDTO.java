package ru.vladislav.bekrenev.ticketapiservice.dto.kafkaevent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCreatedEventDTO <T>{

    @JsonProperty("eventId")
    private String eventId;

    @JsonProperty("correlationId")
    private String correlationId;

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("payload")
    private T payload;

}
