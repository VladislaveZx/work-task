package ru.vladislav.bekrenev.ticketapiservice.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.vladislav.bekrenev.ticketapiservice.dto.kafkaevent.TicketCreatedEventDTO;
import ru.vladislav.bekrenev.ticketapiservice.dto.kafkaevent.TicketPayload;
import ru.vladislav.bekrenev.ticketapiservice.service.KafkaProducer;

import ru.vladislav.bekrenev.ticketapiservice.util.json.JsonUtil;

@Component
@RequiredArgsConstructor
public class TicketEventPublisher {

    private final KafkaProducer kafkaProducer;
    private final JsonUtil jsonUtil;

    public Mono<Void> publishTicketCreated(TicketCreatedEventDTO<TicketPayload> event) {
        return Mono.fromCallable(() -> jsonUtil.toJson(event))
                .flatMap(json -> kafkaProducer.send(
                        "tickets.created",
                        event.getPayload().getId(),
                        json
                ));
    }
}
