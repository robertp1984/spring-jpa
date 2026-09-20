package org.softwarecave.springjpa.outbox.service.dispatch;

import lombok.extern.slf4j.Slf4j;
import org.softwarecave.springjpa.outbox.model.MessageType;
import org.softwarecave.springjpa.outbox.model.Outbox;
import org.softwarecave.springjpa.outbox.model.Status;
import org.softwarecave.springjpa.outbox.service.InvalidOutboxDataException;
import org.softwarecave.springjpa.outbox.service.OutboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final Map<MessageType, OutboxDispatcherStrategy> dispatcherStrategies;
    private final long ackTimeoutMillis;
    private final int batchSize;

    public OutboxDispatcher(OutboxRepository outboxRepository,
                            @Value("${app.outbox.sender.ack-timeout}") long ackTimeoutMillis,
                            @Value("${app.outbox.sender.batch-size}") int batchSize) {
        this.outboxRepository = outboxRepository;
        this.dispatcherStrategies = new HashMap<>();
        this.ackTimeoutMillis = ackTimeoutMillis;
        this.batchSize = batchSize;
    }

    @Autowired(required = false)
    public void setDispatcherStrategies(List<OutboxDispatcherStrategy> dispatcherStrategies) {

        for (var dispatcherStrategy : dispatcherStrategies) {
            var prevValue = this.dispatcherStrategies.putIfAbsent(dispatcherStrategy.getMessageType(), dispatcherStrategy);
            if (prevValue != null) {
                throw new InvalidOutboxDataException("There are conflicting Outbox dispatchers strategies with the same message type");
            }
        }
    }

    @Scheduled(fixedDelayString = "${app.outbox.sender.delay}", timeUnit = TimeUnit.MILLISECONDS)
    @Transactional(value = "transactionManager")
    public void process() {
        // Method findByStatus must use pessimistic locking to prevent multiple instance of this application
        // from processing the same rows at the same time which could result in sending the same message multiple times.
        var entryList = outboxRepository.findByStatus(Status.NEW,
                PageRequest.of(0, batchSize, Sort.by(Sort.Order.asc("createdDate"))));
        log.info("Fetched {} entries from outbox to process", entryList.getContent().size());

        var futureList = sendToKafka(entryList);

        waitForKafkaAcks(futureList, entryList);
    }

    private void waitForKafkaAcks(ArrayList<CompletableFuture<SendResult<String, ?>>> futureList, Page<Outbox> entryList) {
        boolean interrupted = false;

        //TODO: fix monitoring of Futures
        CompletableFuture.allOf(futureList.toArray(CompletableFuture[]::new))
                .orTimeout(ackTimeoutMillis, TimeUnit.MILLISECONDS)
                .join();
        for (int i = 0; i < futureList.size(); i++) {
            var future = futureList.get(i);
            var entry = entryList.getContent().get(i);
            try {
                future.get(); // ignore the result

                updateStatusAsSent(entry);
            } catch (InterruptedException e) {
                interrupted = true;
                log.error("Interrupted while waiting for Kafka ack for outbox entry with id={}", entry.getId(), e);
                // Ignore the interrupt for now and keep processing as usual because we cannot leave the inconsistent state
            } catch (ExecutionException | CancellationException e) {
                log.error("Failed sending the message with id={} from outbox", entry.getId(), e);
            }
        }


        // restore the interrupted flag if wa interrupted
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private void updateStatusAsSent(Outbox entry) {
        log.info("Set the status of outbox id={} to SENT", entry.getAggregateId());
        entry.setStatus(Status.SENT);
    }

    private ArrayList<CompletableFuture<SendResult<String, ?>>> sendToKafka(Page<Outbox> entryList) {
        ArrayList<CompletableFuture<SendResult<String, ?>>> futureList = new ArrayList<>();
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
