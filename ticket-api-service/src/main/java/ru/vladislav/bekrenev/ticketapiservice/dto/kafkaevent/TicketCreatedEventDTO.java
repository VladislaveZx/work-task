package ru.vladislav.bekrenev.ticketapiservice.dto.kafkaevent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
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
