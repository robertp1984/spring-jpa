package org.softwarecave.springjpa.asset.messaging.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.softwarecave.common.avro.AssetEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.softwarecave.springjpa.messaging.KafkaConsumerConfig.ASSET_CONTAINER_FACTORY;
import static org.softwarecave.springjpa.common.messaging.KafkaProperties.MESSAGE_ID;

@Service
@Slf4j
@RequiredArgsConstructor
public class IncomingAssetConsumer {

    private final IncomingAssetProcessor assetProcessor;

    @KafkaListener(topics = "${app.asset.incoming.topic-name}",
            containerFactory = ASSET_CONTAINER_FACTORY)
    public void consumeAsset(@Payload AssetEvent event,
                             @Header(MESSAGE_ID) String messageIdString) {
        log.debug("Received incoming asset with messageId={} {}", messageIdString, event);

        // The listener cannot convert bytes to UUID, so we need to convert it here
        UUID messageId = UUID.fromString(messageIdString);
        assetProcessor.handleIncomingAsset(event, messageId);
    }


}
