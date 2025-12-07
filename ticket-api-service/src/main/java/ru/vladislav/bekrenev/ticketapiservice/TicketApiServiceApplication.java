package ru.vladislav.bekrenev.ticketapiservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableWebFlux
public class TicketApiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketApiServiceApplication.class, args);
    }

}
