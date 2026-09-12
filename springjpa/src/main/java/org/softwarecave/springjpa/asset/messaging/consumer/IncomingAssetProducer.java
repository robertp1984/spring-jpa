package org.softwarecave.springjpa.asset.messaging.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.softwarecave.common.avro.Asset;
import org.softwarecave.common.avro.AssetAction;
import org.softwarecave.common.avro.AssetEvent;
import org.softwarecave.springjpa.common.UUIDGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.random.RandomGeneratorFactory;

import static org.softwarecave.springjpa.common.messaging.KafkaProperties.MESSAGE_ID;


@Service
@Slf4j
public class IncomingAssetProducer {

    private static final int BOUND = 100000;

    private final KafkaTemplate<String, AssetEvent> kafkaTemplate;
    private final String topicName;
    private final String nameFormat;
    private final String descriptionFormat;

    public IncomingAssetProducer(KafkaTemplate<String, AssetEvent> kafkaTemplate,
                                 @Value("${app.asset.incoming.topic-name}") String topicName,
                                 @Value("${app.asset.incoming.name-format}") String nameFormat,
                                 @Value("${app.asset.incoming.description-format}") String descriptionFormat) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
        this.nameFormat = nameFormat;
        this.descriptionFormat = descriptionFormat;
    }

    @Scheduled(initialDelay = 10000, fixedRate = 5000)
    @Transactional(value = "transactionManager")
    public void generateNewAsset() {
        var rand = RandomGeneratorFactory.getDefault().create();

        int number = rand.nextInt(BOUND);
        String name = nameFormat.formatted(number);
        String desc = descriptionFormat.formatted(number);
        var assetEvent = AssetEvent.newBuilder()
                .setAsset(Asset.newBuilder()
                        .setId(UUIDGenerator.get())
                        .setName(name)
                        .setDescription(desc)
                        .build())
                .setAction(AssetAction.ADD)
                .build();

        var producerRecord = new ProducerRecord<>(topicName, assetEvent.getAsset().getId().toString(), assetEvent);
        producerRecord.headers().add(MESSAGE_ID, UUIDGenerator.get().toString().getBytes(StandardCharsets.UTF_8));

        kafkaTemplate.send(producerRecord);
        log.debug("Sent incoming asset {} to topic {}", assetEvent, topicName);
    }
}
