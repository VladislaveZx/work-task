package ru.vladislav.bekrenev.ticketprocessorservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class TicketProcessorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketProcessorServiceApplication.class, args);
    }

}
