package ru.vladislav.bekrenev.ticketapiservice.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.vladislav.bekrenev.ticketapiservice.entity.Ticket;
import ru.vladislav.bekrenev.ticketapiservice.entity.TicketStatus;


@Repository
public interface TicketRepository extends ReactiveCrudRepository<Ticket, Long> {

    @Query("select * from tickets where status = :status limit :size offset :offset")
    Flux<Ticket> findByStatusPaged(TicketStatus status, Integer size, Integer offset);

    @Query("select * from tickets limit :size offset :offset")
    Flux<Ticket> findAllPaged(Integer size, Integer offset);
}
