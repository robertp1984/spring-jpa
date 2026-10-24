package org.softwarecave.springjpa.asset.messaging.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.softwarecave.common.avro.AssetAction;
import org.softwarecave.common.avro.AssetEvent;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class IncomingAssetProducerTest {

    @Mock
    private KafkaTemplate<String, AssetEvent> kafkaTemplate;

    @Test
    void testGenerateNewAsset() {
        // given
        var producer = new IncomingAssetProducer(kafkaTemplate, "topic", "Name %s", "Desc %s");

        ArgumentCaptor<ProducerRecord<String, AssetEvent>> argumentCaptor =
                ArgumentCaptor.forClass(ProducerRecord.class);
        when(kafkaTemplate.send(argumentCaptor.capture())).thenReturn(CompletableFuture.completedFuture(null));

        // when
        producer.generateNewAsset();

        // then
        ProducerRecord<String, AssetEvent> producerRecord = argumentCaptor.getValue();
        String key = producerRecord.key();
        AssetEvent value = producerRecord.value();

        assertThat(key).isNotEmpty().matches(IncomingAssetProducerTest::isValidUUID);
        assertThat(value.getAction()).isEqualTo(AssetAction.ADD);
        assertThat(value.getAsset().getId()).isNotNull();
        assertThat(value.getAsset().getName()).isNotBlank();
        assertThat(value.getAsset().getDescription()).isNotBlank();
    }

    private static boolean isValidUUID(String v) {
        UUID.fromString(v);
        return true;
    }
}
