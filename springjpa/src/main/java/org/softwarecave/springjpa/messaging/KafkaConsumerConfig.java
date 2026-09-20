package org.softwarecave.springjpa.messaging;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.softwarecave.common.avro.AssetEvent;
import org.softwarecave.springjpa.messaging.consumer.NonRetryableException;
import org.softwarecave.springjpa.messaging.consumer.RetryableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;

@Configuration
public class KafkaConsumerConfig {

    public static final String ASSET_CONTAINER_FACTORY = "assetContainerFactory";

    @Bean
    public ConsumerFactory<String, SpecificRecord> consumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id}") String groupId,
            @Value("${spring.kafka.consumer.properties.schema.registry.url}") String schemaRegistryUrl,
            @Value("${spring.kafka.consumer.properties.isolation.level}") String isolationLevel
    ) {
        var config = new HashMap<String, Object>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class.getName());
        config.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class.getName());
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, KafkaAvroDeserializer.class.getName());
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put("schema.registry.url", schemaRegistryUrl);
        config.put("specific.avro.reader", "true");
        config.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, isolationLevel);

        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SpecificRecord> assetContainerFactory(ConsumerFactory<String, SpecificRecord> consumerFactory,
                                                                                             KafkaTemplate<String, Object> kafkaTemplate) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, SpecificRecord>();
        factory.setConsumerFactory(consumerFactory);

        // default error handler sending unprocessable messages to DLT
        var errorHandler = new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaTemplate),
                new FixedBackOff(1000, 3));
        errorHandler.addRetryableExceptions(RetryableException.class);
        errorHandler.addNotRetryableExceptions(NonRetryableException.class);
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}
