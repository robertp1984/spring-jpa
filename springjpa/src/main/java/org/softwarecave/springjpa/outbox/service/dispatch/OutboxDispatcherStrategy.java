package org.softwarecave.springjpa.outbox.service.dispatch;

import org.softwarecave.springjpa.outbox.model.MessageType;
import org.softwarecave.springjpa.outbox.model.Outbox;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

public interface OutboxDispatcherStrategy {
    CompletableFuture<SendResult<String, ?>> send(Outbox outbox);

    MessageType getMessageType();
}
