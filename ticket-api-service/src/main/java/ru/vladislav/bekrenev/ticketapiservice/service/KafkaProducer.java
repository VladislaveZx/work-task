package ru.vladislav.bekrenev.ticketapiservice.service;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import ru.vladislav.bekrenev.ticketapiservice.annotation.LogExecution;

@Component
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaSender<String, String> kafkaSender;


    @LogExecution
    public Mono<Void> send(String topic, String key, String value) {

        SenderRecord<String, String, String> record =
                SenderRecord.create(new ProducerRecord<>(topic, key, value), key);

        return kafkaSender.send(Mono.just(record))
                .doOnNext(r -> System.out.println("Sent: " + r.correlationMetadata()))
                .then();
    }
}
