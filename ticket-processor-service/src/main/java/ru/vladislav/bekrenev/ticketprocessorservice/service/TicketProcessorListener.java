package ru.vladislav.bekrenev.ticketprocessorservice.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;
import ru.vladislav.bekrenev.ticketprocessorservice.dto.TicketCreatedEventDTO;
import ru.vladislav.bekrenev.ticketprocessorservice.entity.ProcessedTicket;
import ru.vladislav.bekrenev.ticketprocessorservice.repository.ProcessedTicketRepository;
import ru.vladislav.bekrenev.ticketprocessorservice.util.json.JsonUtil;
import ru.vladislav.bekrenev.ticketprocessorservice.util.mapper.TicketMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketProcessorListener {

    private final KafkaReceiver<String, String> kafkaReceiver;
    private final JsonUtil jsonUtil;
    private final ProcessedTicketRepository repository;
    private final TicketMapper ticketMapper;

    @PostConstruct
    public void startListening() {
        kafkaReceiver.receive()
                .concatMap(this::handleRecord)
                .subscribe(
                        unused -> {},
                        err -> System.err.println("Kafka listener error: " + err.getMessage())
                );
    }

    private Mono<Void> handleRecord(ReceiverRecord<String, String> record) {
        return Mono.fromCallable(() ->
                        jsonUtil.toObject(record.value(), TicketCreatedEventDTO.class)
                )
                .flatMap(dto -> {
                    String correlationId = dto.getCorrelationId();
                    MDC.put("correlationId", correlationId);

                    return repository.existsById(UUID.fromString(dto.getPayload().getId()))
                            .flatMap(exists -> {
                                if (exists) {
                                    record.receiverOffset().acknowledge();
                                    return Mono.empty();
                                } else {
                                    ProcessedTicket ticket = ticketMapper.mapToProcessedTicket(dto.getPayload());
                                    return repository.save(ticket)
                                            .doOnSuccess(saved -> record.receiverOffset().acknowledge())
                                            .then();
                                }
                            });
                })
                .doFinally(sig -> MDC.clear());
    }
}

//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class TicketProcessorListener {
//
//    private final JsonUtil jsonUtil;
//    private final ProcessedTicketRepository repository;
//    private final TicketMapper ticketMapper;
//
//    @KafkaListener(
//            topics = "tickets.created",
//            groupId = "ticket-processor-group",
//            containerFactory = "kafkaListenerContainerFactory"
//    )
//    public void listen(String message, Acknowledgment ack) {
//
//        TicketCreatedEventDTO event =
//                jsonUtil.toObject(message, TicketCreatedEventDTO.class);
//
//        String correlationId = event.getCorrelationId();
//        MDC.put("correlationId", correlationId);
//
//        try {
//            UUID ticketId = UUID.fromString(event.getPayload().getId());
//
//            boolean exists = repository.existsById(ticketId).block();
//
//            if (exists) {
//                log.info("Ticket already processed, skip. id={}", ticketId);
//                ack.acknowledge();
//                return;
//            }
//
//            ProcessedTicket ticket =
//                    ticketMapper.mapToProcessedTicket(event.getPayload());
//
//            repository.save(ticket).block();
//
//            ack.acknowledge();
//            log.info("Ticket processed successfully. id={}", ticketId);
//
//        } catch (Exception e) {
//            log.error("Failed to process kafka message", e);
//        } finally {
//            MDC.clear();
//        }
//    }
//}
