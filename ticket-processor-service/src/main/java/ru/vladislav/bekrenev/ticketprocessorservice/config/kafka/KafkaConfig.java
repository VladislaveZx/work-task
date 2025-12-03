package ru.vladislav.bekrenev.ticketprocessorservice.config.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    @Value("${spring.kafka.consumer.enable-auto-commit}")
    private boolean enableAutoCommit;

    @Value("${spring.kafka.consumer.isolation-level}")
    private String isolationLevel;

    @Value("${spring.kafka.consumer.max-poll-records}")
    private int maxPollRecords;

    @Value("${spring.kafka.consumer.fetch-min-bytes}")
    private int fetchMinBytes;

    @Value("${spring.kafka.consumer.fetch-max-wait-ms}")
    private int fetchMaxWaitMs;

    @Value("${spring.kafka.consumer.key-deserializer}")
    private String keyDeserializer;

    @Value("${spring.kafka.consumer.value-deserializer}")
    private String valueDeserializer;

    @Value("${app.kafka.topic.tickets-created}")
    private String topic;

    @Bean
    public ReceiverOptions<String, String> receiverOptions() {

        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, groupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer,
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit,
                ConsumerConfig.ISOLATION_LEVEL_CONFIG, isolationLevel,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset,
                ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords,
                ConsumerConfig.FETCH_MIN_BYTES_CONFIG, fetchMinBytes,
                ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, fetchMaxWaitMs
        );

        return ReceiverOptions.<String, String>create(props)
                .subscription(List.of(topic))
                .commitInterval(Duration.ZERO);
    }

    @Bean
    public KafkaReceiver<String, String> kafkaReceiver(
            ReceiverOptions<String, String> receiverOptions
    ) {
        return KafkaReceiver.create(receiverOptions);
    }
}

//@Configuration
//@EnableKafka
//public class KafkaConsumerConfig {
//
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, String>
//    kafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory) {
//
//        ConcurrentKafkaListenerContainerFactory<String, String> factory =
//                new ConcurrentKafkaListenerContainerFactory<>();
//
//        factory.setConsumerFactory(consumerFactory);
//        factory.setConcurrency(1);
//        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
//
//        return factory;
//    }
//}