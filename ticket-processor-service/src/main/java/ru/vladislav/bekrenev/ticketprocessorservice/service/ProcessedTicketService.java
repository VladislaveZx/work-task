package ru.vladislav.bekrenev.ticketprocessorservice.service;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vladislav.bekrenev.ticketprocessorservice.annotation.LogExecution;
import ru.vladislav.bekrenev.ticketprocessorservice.dto.ProcessedTicketView;
import ru.vladislav.bekrenev.ticketprocessorservice.dto.StatsSummaryDTO;
import ru.vladislav.bekrenev.ticketprocessorservice.entity.ProcessedTicket;
import ru.vladislav.bekrenev.ticketprocessorservice.repository.ProcessedTicketRepository;

import java.util.HashMap;
import java.util.Map;

public interface ProcessedTicketService {

    public Mono<ProcessedTicket> save(ProcessedTicket ticket);

    public Mono<PageImpl<ProcessedTicketView>> getProcessedTickets(String status, Pageable pageable);

    public Mono<StatsSummaryDTO> getStatsSummary();

}