package ru.vladislav.bekrenev.ticketprocessorservice.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vladislav.bekrenev.ticketprocessorservice.dto.ProcessedTicketView;
import ru.vladislav.bekrenev.ticketprocessorservice.entity.ProcessedTicket;

import java.util.UUID;


@Repository
public interface ProcessedTicketRepository extends ReactiveCrudRepository<ProcessedTicket, UUID> {

    @Query("SELECT * FROM processed_tickets WHERE status = :status ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<ProcessedTicketView> findByStatusWithPagination(String status, Integer limit, Long offset);

    @Query("SELECT * FROM processed_tickets ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<ProcessedTicketView> findAllWithPagination(Integer limit, Long offset);

    @Query("SELECT COUNT(*) FROM processed_tickets WHERE status = :status")
    Mono<Long> countByStatus(String status);

    @Query("SELECT COUNT(*) FROM processed_tickets")
    Mono<Long> countAll();

    @Query("SELECT status, COUNT(*) as count FROM processed_tickets GROUP BY status")
    Flux<StatusCountProjection> countByStatusGroup();

    @Query("SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (processed_at - created_at))), 0) " +
            "FROM processed_tickets WHERE processed_at IS NOT NULL")
    Mono<Double> findAverageProcessingTimeSeconds();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class StatusCountProjection {
        private String status;
        private Long count;

    }

}