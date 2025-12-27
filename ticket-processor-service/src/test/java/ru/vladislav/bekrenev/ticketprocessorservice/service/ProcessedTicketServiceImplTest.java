package ru.vladislav.bekrenev.ticketprocessorservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.vladislav.bekrenev.ticketprocessorservice.config.metric.MetricConfig;
import ru.vladislav.bekrenev.ticketprocessorservice.dto.ProcessedTicketView;
import ru.vladislav.bekrenev.ticketprocessorservice.dto.StatsSummaryDTO;
import ru.vladislav.bekrenev.ticketprocessorservice.entity.ProcessedTicket;
import ru.vladislav.bekrenev.ticketprocessorservice.repository.ProcessedTicketRepository;
import ru.vladislav.bekrenev.ticketprocessorservice.service.impl.ProcessedTicketServiceImpl;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessedTicketServiceImplTest {

    @Mock
    private ProcessedTicketRepository repository;

    @Mock
    private MetricConfig metricConfig;

    @InjectMocks
    private ProcessedTicketServiceImpl service;

    @Test
    void save_success() {
        ProcessedTicket ticket = ProcessedTicket.builder()
                .id(UUID.randomUUID())
                .status("DONE")
                .build();

        when(repository.save(ticket)).thenReturn(Mono.just(ticket));

        StepVerifier.create(service.save(ticket))
                .expectNext(ticket)
                .verifyComplete();

        verify(repository).save(ticket);
    }

    @Test
    void save_error() {
        ProcessedTicket ticket = ProcessedTicket.builder().build();

        when(repository.save(any()))
                .thenReturn(Mono.error(new RuntimeException("db error")));

        StepVerifier.create(service.save(ticket))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void getProcessedTickets_withoutStatus() {
        var pageable = PageRequest.of(0, 2);

        var view = ProcessedTicketView.builder()
                .id(UUID.randomUUID())
                .status("DONE")
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findAllWithPagination(2, 0L))
                .thenReturn(Flux.just(view));
        when(repository.countAll())
                .thenReturn(Mono.just(1L));

        StepVerifier.create(service.getProcessedTickets(null, pageable))
                .assertNext(page -> {
                    assert page.getTotalElements() == 1;
                    assert page.getContent().size() == 1;
                })
                .verifyComplete();
    }

    @Test
    void getProcessedTickets_withStatus() {
        var pageable = PageRequest.of(0, 1);

        var view = ProcessedTicketView.builder()
                .id(UUID.randomUUID())
                .status("FAILED")
                .build();

        when(repository.findByStatusWithPagination("FAILED", 1, 0L))
                .thenReturn(Flux.just(view));
        when(repository.countByStatus("FAILED"))
                .thenReturn(Mono.just(1L));

        StepVerifier.create(service.getProcessedTickets("FAILED", pageable))
                .assertNext(page -> {
                    assert page.getTotalElements() == 1;
                    assert page.getContent().get(0).getStatus().equals("FAILED");
                })
                .verifyComplete();
    }

    @Test
    void getStatsSummary_success() {
        var doneProjection = new ProcessedTicketRepository.StatusCountProjection("DONE", 3L);
        var newProjection = new ProcessedTicketRepository.StatusCountProjection("NEW", 2L);

        when(repository.countAll()).thenReturn(Mono.just(5L));
        when(repository.countByStatusGroup()).thenReturn(Flux.just(doneProjection, newProjection));
        when(repository.findAverageProcessingTimeSeconds())
                .thenReturn(Mono.just(10.0));

        StepVerifier.create(service.getStatsSummary())
                .assertNext(stats -> {
                    assert stats.getTotalTickets() == 5L;
                    assert stats.getByStatus().get("DONE") == 3L;
                    assert stats.getByStatus().get("NEW") == 2L;
                    assert stats.getAvgProcessingTimeSeconds() == 10.0;
                })
                .verifyComplete();
    }

    @Test
    void getStatsSummary_emptyData() {
        when(repository.countAll()).thenReturn(Mono.just(0L));
        when(repository.countByStatusGroup()).thenReturn(Flux.empty());
        when(repository.findAverageProcessingTimeSeconds())
                .thenReturn(Mono.empty());

        StepVerifier.create(service.getStatsSummary())
                .assertNext(stats -> {
                    assert stats.getTotalTickets() == 0L;
                    assert stats.getByStatus().isEmpty();
                    assert stats.getAvgProcessingTimeSeconds() == 0.0;
                })
                .verifyComplete();
    }

    @Test
    void getStatsSummary_nullAvgTime() {
        var newProjection = new ProcessedTicketRepository.StatusCountProjection("NEW", 5L);

        when(repository.countAll()).thenReturn(Mono.just(5L));
        when(repository.countByStatusGroup()).thenReturn(Flux.just(newProjection));
        when(repository.findAverageProcessingTimeSeconds())
                .thenReturn(Mono.just(0.0));

        StepVerifier.create(service.getStatsSummary())
                .assertNext(stats -> {
                    assert stats.getTotalTickets() == 5L;
                    assert stats.getByStatus().get("NEW") == 5L;
                    assert stats.getAvgProcessingTimeSeconds() == 0.0;
                })
                .verifyComplete();
    }

    @Test
    void getStatsSummary_withMultipleStatuses() {
        var doneProjection = new ProcessedTicketRepository.StatusCountProjection("DONE", 10L);
        var failedProjection = new ProcessedTicketRepository.StatusCountProjection("FAILED", 5L);
        var newProjection = new ProcessedTicketRepository.StatusCountProjection("NEW", 15L);

        when(repository.countAll()).thenReturn(Mono.just(30L));
        when(repository.countByStatusGroup()).thenReturn(Flux.just(doneProjection, failedProjection, newProjection));
        when(repository.findAverageProcessingTimeSeconds())
                .thenReturn(Mono.just(12.5));

        StepVerifier.create(service.getStatsSummary())
                .assertNext(stats -> {
                    StatsSummaryDTO result = stats;
                    assert result.getTotalTickets() == 30L;
                    assert result.getByStatus().size() == 3;
                    assert result.getByStatus().get("DONE") == 10L;
                    assert result.getByStatus().get("FAILED") == 5L;
                    assert result.getByStatus().get("NEW") == 15L;
                    assert result.getAvgProcessingTimeSeconds() == 12.5;
                })
                .verifyComplete();
    }
}