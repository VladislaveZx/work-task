package ru.vladislav.bekrenev.ticketapiservice.config.micrometer;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class MetricConfig {

    public static final String TICKET_API_TICKETS_CREATED_TOTAL = "ticket_api_tickets_created_total";
    public static final String TICKET_API_DATABASE_SAVE_TIME = "ticket_api_database_save_time";
    public static final String TICKET_API_KAFKA_MESSAGES_SENT_TOTAL = "ticket_api_kafka_messages_sent_total";
    public static final String TICKET_API_KAFKA_AVAILABLE = "ticket_api_kafka_available";
    public static final String TICKET_API_REQUEST_LATENCY = "ticket_api_request_latency";
    public static final String TICKET_API_TICKETS_NEW_COUNT = "ticket_api_tickets_new_count";
    public static final String TICKET_API_DATABASE_ERROR = "ticket_api_database_error";

    private final MeterRegistry meterRegistry;

    private final AtomicInteger newTicketsCount = new AtomicInteger(0);

    @PostConstruct
    public void initMetrics() {
        Counter.builder(TICKET_API_DATABASE_ERROR)
                .description("databases error")
                .register(meterRegistry);

        Counter.builder(TICKET_API_TICKETS_CREATED_TOTAL)
                .description("Total number of tickets created")
                .register(meterRegistry);

        Counter.builder(TICKET_API_KAFKA_MESSAGES_SENT_TOTAL)
                .description("Total number of Kafka messages sent successfully")
                .register(meterRegistry);

        Counter.builder(TICKET_API_KAFKA_AVAILABLE)
                .description("Count of Kafka connection errors")
                .register(meterRegistry);

        Gauge.builder(TICKET_API_TICKETS_NEW_COUNT, newTicketsCount, AtomicInteger::get)
                .description("Current number of tickets with NEW status")
                .register(meterRegistry);

        Timer.builder(TICKET_API_DATABASE_SAVE_TIME)
                .description("Time taken to save ticket to database")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        Timer.builder(TICKET_API_REQUEST_LATENCY)
                .description("HTTP request latency for ticket operations")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }


    //todo - убрать методы
    public void incrementDatabaseErrors(){
        meterRegistry.counter(TICKET_API_DATABASE_ERROR).increment();
    }

    public void incrementTicketsCreated() {
        meterRegistry.counter(TICKET_API_TICKETS_CREATED_TOTAL).increment();
    }

    public void incrementKafkaMessagesSent() {
        meterRegistry.counter(TICKET_API_KAFKA_MESSAGES_SENT_TOTAL).increment();
    }

    public void incrementKafkaError() {
        meterRegistry.counter(TICKET_API_KAFKA_AVAILABLE).increment();
    }

    public void setNewTicketsCount(int count) {
        newTicketsCount.set(count);
    }

    public void incrementNewTickets() {
        newTicketsCount.incrementAndGet();
    }

    public Timer.Sample startDatabaseSaveTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordDatabaseSaveTime(Timer.Sample sample) {
        sample.stop(meterRegistry.timer(TICKET_API_DATABASE_SAVE_TIME));
    }

    public Timer.Sample startRequestLatencyTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordRequestLatency(Timer.Sample sample) {
        sample.stop(meterRegistry.timer(TICKET_API_REQUEST_LATENCY));
    }
}
