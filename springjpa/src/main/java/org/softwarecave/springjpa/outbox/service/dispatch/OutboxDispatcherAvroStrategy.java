package org.softwarecave.springjpa.outbox.service.dispatch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.softwarecave.springjpa.outbox.model.AggregateType;
import org.softwarecave.springjpa.outbox.model.MessageType;
import org.softwarecave.springjpa.outbox.model.Outbox;
import org.softwarecave.springjpa.outbox.service.InvalidOutboxDataException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static org.softwarecave.springjpa.outbox.tools.AvroTools.fromBytes;

@RequiredArgsConstructor
@Service
@Slf4j
public class OutboxDispatcherAvroStrategy implements OutboxDispatcherStrategy {

    private final KafkaTemplate<String, SpecificRecord> kafkaTemplate;

    @Override
    public CompletableFuture<SendResult<String, ?>> send(Outbox outbox) {
        return sendToKafka(outbox);
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.AVRO;
    }

    private Class<? extends SpecificRecord> getAvroClass(Outbox entry) {
        AggregateType aggregateType = entry.getAggregateType();
        if (aggregateType != null) {
            return aggregateType.getAvroClass();
        } else {
            throw new InvalidOutboxDataException("Null aggregate type for entry %s".formatted(entry.getId()));
        }
    }

    public CompletableFuture<SendResult<String, ?>> sendToKafka(Outbox value) {
        try {
            var avroClass = getAvroClass(value);
            var avroObject = fromBytes(value.getPayloadBytes(), avroClass);

            return kafkaTemplate.send(value.getTopic(), value.getAggregateId(), avroObject)
                    .thenApply(a ->  a);
        } catch (Exception e) {
            throw new RuntimeException(e); //TODO:
        }
    }
}
