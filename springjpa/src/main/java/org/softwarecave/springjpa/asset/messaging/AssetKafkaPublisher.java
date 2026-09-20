package org.softwarecave.springjpa.asset.messaging;

import lombok.extern.slf4j.Slf4j;
import org.softwarecave.common.avro.AssetEvent;
import org.softwarecave.springjpa.asset.converters.AssetAvroConverter;
import org.softwarecave.springjpa.asset.model.Asset;
import org.softwarecave.springjpa.outbox.model.AggregateType;
import org.softwarecave.springjpa.outbox.service.queue.AvroOutboxService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AssetKafkaPublisher {

    private final String topicName;
    private final AssetAvroConverter assetAvroConverter;
    private final AvroOutboxService avroOutboxService;

    public AssetKafkaPublisher(@Value("${app.asset.kafka-publisher.topic-name}") String topicName,
                               AvroOutboxService avroOutboxService,
                               AssetAvroConverter assetAvroConverter) {
        this.topicName = topicName;

        this.avroOutboxService = avroOutboxService;
        this.assetAvroConverter = assetAvroConverter;
    }

    public void sendAdded(Asset asset) {
        AssetEvent event = assetAvroConverter.toAssetAvro(asset);
        log.info("Sending the event {} to topic {}", event, topicName);
        avroOutboxService.send(topicName, event.getAsset().getId().toString(), event, AggregateType.ASSET);
    }

}
