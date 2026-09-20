package org.softwarecave.springjpa.outbox.service.dispatch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.softwarecave.springjpa.outbox.model.MessageType;
import org.softwarecave.springjpa.outbox.model.Outbox;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
@Slf4j
public class OutboxDispatcherJsonStrategy implements OutboxDispatcherStrategy {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public CompletableFuture<SendResult<String, ?>> send(Outbox outbox) {
        return kafkaTemplate.send(outbox.getTopic(), outbox.getAggregateId(), outbox.getPayloadString())
                .thenApply(a -> a);
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.JSON;
    }

}
