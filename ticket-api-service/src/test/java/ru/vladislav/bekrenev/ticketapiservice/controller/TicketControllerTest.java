package ru.vladislav.bekrenev.ticketapiservice.controller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import ru.vladislav.bekrenev.ticketapiservice.dto.TicketCreateDTO;
import ru.vladislav.bekrenev.ticketapiservice.entity.TicketStatus;
import ru.vladislav.bekrenev.ticketapiservice.repository.TicketRepository;


@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
public class TicketControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TicketRepository ticketRepository;

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("api_service_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @BeforeEach
    void setUp() {
        System.setProperty("spring.r2dbc.url",
                String.format("r2dbc:postgresql://%s:%d/%s",
                        postgresContainer.getHost(),
                        postgresContainer.getFirstMappedPort(),
                        postgresContainer.getDatabaseName()));
        System.setProperty("spring.r2dbc.username", postgresContainer.getUsername());
        System.setProperty("spring.r2dbc.password", postgresContainer.getPassword());

        System.setProperty("spring.kafka.bootstrap-servers", kafkaContainer.getBootstrapServers());
    }

    private TicketCreateDTO createTestTicketDTO() {
        return TicketCreateDTO.builder()
                .title("Test Ticket")
                .description("This is a test")
                .category("TECH")
                .build();
    }

    @Test
    void createTicketTest() {
        TicketCreateDTO dto = createTestTicketDTO();

        webTestClient.post()
                .uri("/api/v1/tickets")
                .header("X-Correlation-Id", "test-corr-id")
                .body(Mono.just(dto), TicketCreateDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals("X-Correlation-Id", "test-corr-id")
                .expectBody()
                .jsonPath("$.title").isEqualTo("Test Ticket")
                .jsonPath("$.category").isEqualTo("TECH");
    }

    @Test
    void getTicketByIdTest() {

        TicketCreateDTO dto = createTestTicketDTO();

        webTestClient.post()
                .uri("/api/v1/tickets")
                .header("X-Correlation-Id", "corr-id-2")
                .body(Mono.just(dto), TicketCreateDTO.class)
                .exchange()
                .expectStatus().isCreated();

        var ticket = ticketRepository.findAll()
                .blockFirst();

        assert ticket != null;

        webTestClient.get()
                .uri("/api/v1/tickets/{id}", ticket.getId())
                .header("X-Correlation-Id", "corr-id-2")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Correlation-Id", "corr-id-2")
                .expectBody()
                .jsonPath("$.title").isEqualTo("Test Ticket")
                .jsonPath("$.category").isEqualTo("TECH");
    }

    @Test
    void getTicketsByStatusTest() {
        TicketCreateDTO dto = createTestTicketDTO();

        webTestClient.post()
                .uri("/api/v1/tickets")
                .header("X-Correlation-Id", "status-corr-id")
                .body(Mono.just(dto), TicketCreateDTO.class)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/tickets/status/{status}")
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build(TicketStatus.NEW))
                .header("X-Correlation-Id", "status-corr-id")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].title").isEqualTo("Test Ticket");
    }

    @Test
    void getAllTicketsTest() {
        TicketCreateDTO dto = createTestTicketDTO();

        webTestClient.post()
                .uri("/api/v1/tickets")
                .header("X-Correlation-Id", "all-corr-id")
                .body(Mono.just(dto), TicketCreateDTO.class)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/tickets")
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .header("X-Correlation-Id", "all-corr-id")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].title").isEqualTo("Test Ticket");
    }
}
