package org.softwarecave.springjpa.outbox.service.dispatch;

import lombok.extern.slf4j.Slf4j;
import org.softwarecave.springjpa.outbox.model.MessageType;
import org.softwarecave.springjpa.outbox.model.Outbox;
import org.softwarecave.springjpa.outbox.model.Status;
import org.softwarecave.springjpa.outbox.service.InvalidOutboxDataException;
import org.softwarecave.springjpa.outbox.service.OutboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class OutboxDispatcher {

    private final OutboxRepository outboxRepository;
    private final OutboxDispatcherBackoff outboxDispatcherBackoff;
    private final Map<MessageType, OutboxDispatcherStrategy> dispatcherStrategies;
    private final long ackTimeoutMillis;
    private final int batchSize;

    public OutboxDispatcher(OutboxRepository outboxRepository,
                            OutboxDispatcherBackoff outboxDispatcherBackoff,
                            @Value("${app.outbox.sender.ack-timeout}") long ackTimeoutMillis,
                            @Value("${app.outbox.sender.batch-size}") int batchSize) {
        this.outboxRepository = outboxRepository;
        this.outboxDispatcherBackoff = outboxDispatcherBackoff;
        this.dispatcherStrategies = new HashMap<>();
        this.ackTimeoutMillis = ackTimeoutMillis;
        this.batchSize = batchSize;
    }

    @Autowired(required = false)
    public void setDispatcherStrategies(List<OutboxDispatcherStrategy> dispatcherStrategies) {

        for (var dispatcherStrategy : dispatcherStrategies) {
            var prevValue = this.dispatcherStrategies.putIfAbsent(dispatcherStrategy.getMessageType(), dispatcherStrategy);
            if (prevValue != null) {
                throw new IllegalStateException("There are conflicting Outbox dispatchers strategies with the same message type");
            }
        }
    }

    @Scheduled(fixedDelayString = "${app.outbox.sender.delay}", timeUnit = TimeUnit.MILLISECONDS)
    @Transactional(value = "transactionManager")
    public void process() {
        // Method findByStatusAndNextAttempt must use pessimistic locking to prevent multiple instance of this application
        // from processing the same rows at the same time which could result in sending the same message multiple times.
        var entryList = outboxRepository.findByStatusAndNextAttempt(Status.NEW, Instant.now(), batchSize);
        log.debug("Fetched {} entries from outbox to process", entryList.size());

        var futureList = sendToKafka(entryList);

        waitForKafkaAcks(futureList, entryList);
    }

    private void waitForKafkaAcks(List<CompletableFuture<SendResult<String, ?>>> futureList, List<Outbox> entryList) {
        waitForAll(futureList);

        boolean interrupted = false;
        for (int i = 0; i < futureList.size(); i++) {
            var future = futureList.get(i);
            var entry = entryList.get(i);
            try {
                future.get(); // just check if finished
                updateStatusAsSent(entry);
            } catch (InterruptedException e) {
                interrupted = true;
                log.error("Interrupted while waiting for Kafka ack for outbox entry with id={}", entry.getId(), e);
                // Ignore the interrupt for now and keep processing as usual because we cannot leave the inconsistent state
            } catch (ExecutionException | CancellationException e) {
                log.error("Failed sending the message with id={} from outbox", entry.getId(), e);
                outboxDispatcherBackoff.onFailure(entry);
            }
        }

        // restore the interrupted flag if was interrupted
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private void waitForAll(List<CompletableFuture<SendResult<String, ?>>> futureList) {
        // Apply the timeout to each future individually and swallow its outcome (success or failure) into a
        // settlement future, so that allOf().join() below waits for every send to settle without throwing as
        // soon as the first one fails or times out. This lets us process each entry's own outcome afterwards
        // instead of aborting the whole batch on the first failure.
        var settlementFutures = futureList.stream()
                .map(future -> future
                        .orTimeout(ackTimeoutMillis, TimeUnit.MILLISECONDS)
                        .handle((result, ex) -> null))
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(settlementFutures).join();
    }

    private void updateStatusAsSent(Outbox entry) {
        log.info("Set the status of outbox id={} to SENT", entry.getId());
        entry.setStatus(Status.SENT);
    }

    private List<CompletableFuture<SendResult<String, ?>>> sendToKafka(List<Outbox> entryList) {
        List<CompletableFuture<SendResult<String, ?>>> futureList = new ArrayList<>();
        for (var entry : entryList) {
            futureList.add(sendToKafka(entry));
        }
        return futureList;
    }

    private CompletableFuture<SendResult<String, ?>> sendToKafka(Outbox outbox) {
        MessageType messageType = outbox.getMessageType();
        var dispatcherStrategy = dispatcherStrategies.get(messageType);
        if (dispatcherStrategy != null) {
            return dispatcherStrategy.send(outbox);
        } else {
            log.error("Unrecognized message type {} for outbox with id={}. Outbox entry will be skipped.", messageType, outbox.getId());
            InvalidOutboxDataException exception = new InvalidOutboxDataException("Unrecognized message type %s for outbox with id=%s."
                    .formatted(messageType, outbox.getId()));
            return CompletableFuture.failedFuture(exception);
        }
    }

}
