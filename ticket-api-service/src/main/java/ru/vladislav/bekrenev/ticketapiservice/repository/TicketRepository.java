package ru.vladislav.bekrenev.ticketapiservice.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vladislav.bekrenev.ticketapiservice.entity.Ticket;
import ru.vladislav.bekrenev.ticketapiservice.entity.TicketStatus;

import java.util.UUID;


@Repository
public interface TicketRepository extends ReactiveCrudRepository<Ticket, UUID> {

    @Query("select * from tickets where status = :status ORDER BY created_at DESC limit :size offset (:page * :size)")
    Flux<Ticket> findByStatusPaged(TicketStatus status, Integer page, Integer size);

    @Query("select * from tickets ORDER BY created_at DESC limit :size offset (:page * :size)")
    Flux<Ticket> findAllPaged(Integer page, Integer size);

}
