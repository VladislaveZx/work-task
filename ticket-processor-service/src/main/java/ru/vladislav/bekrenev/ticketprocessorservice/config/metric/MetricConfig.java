package ru.vladislav.bekrenev.ticketprocessorservice.config.metric;

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

    public static final String TICKET_PROCESSOR_MESSAGES_CONSUMED_TOTAL =
            "ticket_processor_messages_consumed_total";
    public static final String TICKET_PROCESSOR_MESSAGES_FAILED_TOTAL =
            "ticket_processor_messages_failed_total";
    public static final String TICKET_PROCESSOR_DATABASE_ERRORS_TOTAL =
            "ticket_processor_database_errors_total";
    public static final String TICKET_PROCESSOR_MESSAGE_PROCESSING_TIME =
            "ticket_processor_message_processing_time";
    public static final String TICKET_PROCESSOR_DATABASE_SAVE_TIME =
            "ticket_processor_database_save_time";
    public static final String TICKET_PROCESSOR_REQUEST_LATENCY =
            "ticket_processor_request_latency";
    public static final String TICKET_PROCESSOR_KAFKA_AVAILABLE =
            "ticket_processor_kafka_available";

    private final MeterRegistry meterRegistry;

    private final AtomicInteger kafkaAvailable = new AtomicInteger(1);

    @PostConstruct
    public void initMetrics() {

        meterRegistry.counter(TICKET_PROCESSOR_MESSAGES_CONSUMED_TOTAL);
        meterRegistry.counter(TICKET_PROCESSOR_MESSAGES_FAILED_TOTAL);
        meterRegistry.counter(TICKET_PROCESSOR_DATABASE_ERRORS_TOTAL);

        Timer.builder(TICKET_PROCESSOR_MESSAGE_PROCESSING_TIME)
                .description("Time taken to process Kafka message")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        Timer.builder(TICKET_PROCESSOR_DATABASE_SAVE_TIME)
                .description("Time taken to save processed ticket to database")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        Timer.builder(TICKET_PROCESSOR_REQUEST_LATENCY)
                .description("HTTP request latency for processed tickets operations")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        Gauge.builder(
                        TICKET_PROCESSOR_KAFKA_AVAILABLE,
                        kafkaAvailable,
                        AtomicInteger::get
                )
                .description("Kafka connection availability (1=available, 0=unavailable)")
                .register(meterRegistry);
    }

    public void incrementMessagesConsumed() {
        meterRegistry.counter(TICKET_PROCESSOR_MESSAGES_CONSUMED_TOTAL).increment();
    }

    public void incrementMessagesFailed() {
        meterRegistry.counter(TICKET_PROCESSOR_MESSAGES_FAILED_TOTAL).increment();
    }

    public void incrementDatabaseErrors() {
        meterRegistry.counter(TICKET_PROCESSOR_DATABASE_ERRORS_TOTAL).increment();
    }

    public void setKafkaAvailable(boolean available) {
        kafkaAvailable.set(available ? 1 : 0);
    }

    public Timer.Sample startMessageProcessingTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordMessageProcessingTime(Timer.Sample sample) {
        sample.stop(meterRegistry.timer(TICKET_PROCESSOR_MESSAGE_PROCESSING_TIME));
    }

    public Timer.Sample startDatabaseSaveTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordDatabaseSaveTime(Timer.Sample sample) {
        sample.stop(meterRegistry.timer(TICKET_PROCESSOR_DATABASE_SAVE_TIME));
    }

    public Timer.Sample startRequestLatencyTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordRequestLatency(Timer.Sample sample) {
        sample.stop(meterRegistry.timer(TICKET_PROCESSOR_REQUEST_LATENCY));
    }
}
