package org.softwarecave.springjpa.outbox.service.dispatch;

import java.util.concurrent.CompletableFuture;
import org.softwarecave.springjpa.outbox.model.MessageType;
import org.softwarecave.springjpa.outbox.model.Outbox;
import org.springframework.kafka.support.SendResult;

public interface OutboxDispatcherStrategy {
    CompletableFuture<SendResult<String, ?>> send(Outbox outbox);

    MessageType getMessageType();
}
