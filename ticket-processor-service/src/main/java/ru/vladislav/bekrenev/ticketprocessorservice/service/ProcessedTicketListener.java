package ru.vladislav.bekrenev.ticketprocessorservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import reactor.util.context.Context;
import ru.vladislav.bekrenev.ticketprocessorservice.annotation.LogExecution;
import ru.vladislav.bekrenev.ticketprocessorservice.config.metric.MetricConfig;
import ru.vladislav.bekrenev.ticketprocessorservice.dto.TicketCreatedEventDTO;
import ru.vladislav.bekrenev.ticketprocessorservice.entity.ProcessedTicket;
import ru.vladislav.bekrenev.ticketprocessorservice.service.impl.ProcessedTicketServiceImpl;
import ru.vladislav.bekrenev.ticketprocessorservice.util.json.JsonUtil;
import ru.vladislav.bekrenev.ticketprocessorservice.util.mapper.TicketMapper;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessedTicketListener {

    private final JsonUtil jsonUtil;
    private final TicketMapper ticketMapper;
    private final ProcessedTicketServiceImpl ticketService;
    private final MetricConfig metricConfig;

    @KafkaListener(
            topics = "${kafka.topics.tickets-created}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @LogExecution
    public void listen(String message, Acknowledgment acknowledgment) {
        var messageProcessingTimer = metricConfig.startMessageProcessingTimer();

        try {
            TicketCreatedEventDTO dto = jsonUtil.toObject(message, TicketCreatedEventDTO.class);
            String correlationId = dto.getCorrelationId();

            Map<String, String> mdcContext = new HashMap<>();
            mdcContext.put("correlationId", correlationId);

            ticketService.save(createProcessedTicket(dto))
                    .contextWrite(Context.of("mdc-context", mdcContext))
                    .doOnSuccess(saved -> {
                        metricConfig.recordMessageProcessingTime(messageProcessingTimer);
                        metricConfig.incrementMessagesConsumed();
                        acknowledgment.acknowledge();
                    })
                    .doOnError(err -> {
                        metricConfig.recordMessageProcessingTime(messageProcessingTimer);
                        metricConfig.incrementMessagesFailed();
                        acknowledgment.acknowledge();
                    })
                    .subscribe();

        } catch (Exception e) {
            metricConfig.recordMessageProcessingTime(messageProcessingTimer);
            metricConfig.incrementMessagesFailed();
            acknowledgment.acknowledge();
        }
    }

    private ProcessedTicket createProcessedTicket(TicketCreatedEventDTO dto) {
        if (dto == null || dto.getPayload() == null) {
            throw new IllegalArgumentException("Received null DTO or payload");
        }

        String ticketId = dto.getPayload().getId();
        if (ticketId == null || ticketId.isEmpty()) {
            throw new IllegalArgumentException("Ticket ID is null or empty");
        }

        return ticketMapper.mapToProcessedTicket(dto.getPayload());
    }
}