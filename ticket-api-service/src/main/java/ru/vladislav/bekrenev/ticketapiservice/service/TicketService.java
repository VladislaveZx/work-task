package ru.vladislav.bekrenev.ticketapiservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.vladislav.bekrenev.ticketapiservice.repository.TicketRepository;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

}
