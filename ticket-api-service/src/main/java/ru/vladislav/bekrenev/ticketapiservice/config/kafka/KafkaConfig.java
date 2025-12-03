package ru.vladislav.bekrenev.ticketapiservice.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public KafkaSender<String, String> kafkaSender(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.producer.key-serializer}") String keySerializer,
            @Value("${spring.kafka.producer.value-serializer}") String valueSerializer
    ) throws ClassNotFoundException {
        Map<String, Object> props = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, Class.forName(keySerializer),
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, Class.forName(valueSerializer)
        );
        SenderOptions<String, String> senderOptions = SenderOptions.create(props);

        return KafkaSender.create(senderOptions);
    }

    @Bean
    NewTopic createTopic() {
        return TopicBuilder.name("tickets.created")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
