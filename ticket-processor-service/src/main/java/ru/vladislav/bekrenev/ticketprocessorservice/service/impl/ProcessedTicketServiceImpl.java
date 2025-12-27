package ru.vladislav.bekrenev.ticketprocessorservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vladislav.bekrenev.ticketprocessorservice.annotation.LogExecution;
import ru.vladislav.bekrenev.ticketprocessorservice.config.metric.MetricConfig;
import ru.vladislav.bekrenev.ticketprocessorservice.dto.ProcessedTicketView;
import ru.vladislav.bekrenev.ticketprocessorservice.dto.StatsSummaryDTO;
import ru.vladislav.bekrenev.ticketprocessorservice.entity.ProcessedTicket;
import ru.vladislav.bekrenev.ticketprocessorservice.repository.ProcessedTicketRepository;
import ru.vladislav.bekrenev.ticketprocessorservice.service.ProcessedTicketService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessedTicketServiceImpl implements ProcessedTicketService {

    private final ProcessedTicketRepository repository;
    private final MetricConfig metricConfig;

    @Transactional
    @LogExecution
    public Mono<ProcessedTicket> save(ProcessedTicket ticket) {
        var databaseSaveTimer = metricConfig.startDatabaseSaveTimer();

        return Mono.just(ticket)
                .flatMap(t -> repository.save(t)
                        .doOnSuccess(saved -> {
                            metricConfig.recordDatabaseSaveTime(databaseSaveTimer);
                        })
                        .doOnError(err -> {
                            metricConfig.recordDatabaseSaveTime(databaseSaveTimer);
                            metricConfig.incrementDatabaseErrors();
                        }));
    }

    @LogExecution
    public Mono<PageImpl<ProcessedTicketView>> getProcessedTickets(String status, Pageable pageable) {
        var requestLatencyTimer = metricConfig.startRequestLatencyTimer();

        int limit = pageable.getPageSize();
        long offset = pageable.getOffset();

        Flux<ProcessedTicketView> ticketsFlux;
        Mono<Long> countMono;

        if (status != null && !status.isEmpty()) {
            ticketsFlux = repository.findByStatusWithPagination(status, limit, offset);
            countMono = repository.countByStatus(status);
        } else {
            ticketsFlux = repository.findAllWithPagination(limit, offset);
            countMono = repository.countAll();
        }

        return ticketsFlux.collectList()
                .zipWith(countMono)
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()))
                .doOnSuccess(page -> metricConfig.recordRequestLatency(requestLatencyTimer))
                .doOnError(err -> metricConfig.recordRequestLatency(requestLatencyTimer));
    }

    @LogExecution
    public Mono<StatsSummaryDTO> getStatsSummary() {
        var requestLatencyTimer = metricConfig.startRequestLatencyTimer();

        Mono<Double> avgTimeMono =
                repository.findAverageProcessingTimeSeconds()
                        .defaultIfEmpty(0.0);

        return Mono.zip(
                        repository.countAll(),
                        getStatusCounts(),
                        avgTimeMono
                )
                .map(tuple -> StatsSummaryDTO.builder()
                        .totalTickets(tuple.getT1())
                        .byStatus(tuple.getT2())
                        .avgProcessingTimeSeconds(tuple.getT3())
                        .build()
                )
                .doFinally(signal -> metricConfig.recordRequestLatency(requestLatencyTimer));
    }

    @LogExecution
    private Mono<Map<String, Long>> getStatusCounts() {
        return repository.countByStatusGroup()
                .collectMap(
                        ProcessedTicketRepository.StatusCountProjection::getStatus,
                        ProcessedTicketRepository.StatusCountProjection::getCount
                )
                .defaultIfEmpty(new HashMap<>());
    }
}