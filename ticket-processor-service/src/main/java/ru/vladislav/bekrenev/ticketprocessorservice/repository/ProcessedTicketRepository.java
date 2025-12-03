package ru.vladislav.bekrenev.ticketprocessorservice.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.vladislav.bekrenev.ticketprocessorservice.entity.ProcessedTicket;

import java.util.UUID;

public interface ProcessedTicketRepository extends ReactiveCrudRepository<ProcessedTicket, UUID> {

    @Query("select * from ")
    Mono<Boolean> existsById(UUID id);
}
