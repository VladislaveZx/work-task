package ru.vladislav.bekrenev.ticketprocessorservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterOptions;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;
import ru.vladislav.bekrenev.ticketprocessorservice.config.metric.MetricConfig;

import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaHealthIndicator implements HealthIndicator {

    private final KafkaAdmin kafkaAdmin;
    private final MetricConfig metricConfig;

    @Override
    public Health health() {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {

            DescribeClusterOptions options = new DescribeClusterOptions().timeoutMs(5000);

            var clusterDescription = adminClient.describeCluster(options);

            int nodeCount = clusterDescription.nodes().get().size();
            String clusterId = clusterDescription.clusterId().get();

            if (nodeCount > 0) {
                metricConfig.setKafkaAvailable(true);
                return Health.up()
                        .withDetail("service", "Kafka")
                        .withDetail("status", "available")
                        .withDetail("clusterId", clusterId)
                        .withDetail("nodeCount", nodeCount)
                        .build();
            } else {
                metricConfig.setKafkaAvailable(false);
                return Health.down()
                        .withDetail("service", "Kafka")
                        .withDetail("status", "unavailable")
                        .withDetail("error", "No Kafka nodes available")
                        .build();
            }

        } catch (ExecutionException | InterruptedException e) {
            metricConfig.setKafkaAvailable(false);
            log.debug("Kafka health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("service", "Kafka")
                    .withDetail("status", "unavailable")
                    .withDetail("error", e.getCause() != null ? e.getCause().getMessage() : e.getMessage())
                    .build();
        } catch (Exception e) {
            metricConfig.setKafkaAvailable(false);
            log.debug("Kafka health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("service", "Kafka")
                    .withDetail("status", "unavailable")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}