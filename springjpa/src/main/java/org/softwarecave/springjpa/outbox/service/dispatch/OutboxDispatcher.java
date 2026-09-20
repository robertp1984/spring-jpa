package org.softwarecave.springjpa.outbox.service.dispatch;

import lombok.extern.slf4j.Slf4j;
import org.softwarecave.springjpa.outbox.model.MessageType;
import org.softwarecave.springjpa.outbox.model.Outbox;
import org.softwarecave.springjpa.outbox.model.Status;
import org.softwarecave.springjpa.outbox.service.InvalidOutboxDataException;
import org.softwarecave.springjpa.outbox.service.OutboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class OutboxDispatcher {

    private final OutboxRepository outboxRepository;
    private final Map<MessageType, OutboxDispatcherStrategy> dispatcherStrategies;

    public OutboxDispatcher(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
        this.dispatcherStrategies = new HashMap<>();
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
        var entryList = outboxRepository.findByStatus(Status.NEW,
                PageRequest.of(0, 100, Sort.by(Sort.Order.asc("createdDate"))));
        log.info("Fetched {} entries from outbox to process", entryList.getContent().size());

        var futureList = sendToKafka(entryList);

        waitForKafkaAcks(futureList, entryList);
    }

    private void waitForKafkaAcks(ArrayList<CompletableFuture<SendResult<String, ?>>> futureList, Page<Outbox> entryList) {
        for (int i = 0; i < futureList.size(); i++) {
            var future = futureList.get(i);
            try {
                var sendResult = future.get();
                var entry = entryList.getContent().get(i);

                updateStatusAsSent(entry);
            } catch (InterruptedException | ExecutionException e) {
                log.error("Failed sending the message from outbox ", e);
            }
        }
    }

    private void updateStatusAsSent(Outbox entry) {
        log.info("Set the status of outbox entry {} to SENT", entry.getPayloadString());
        entry.setStatus(Status.SENT);
        outboxRepository.save(entry);
    }

    private ArrayList<CompletableFuture<SendResult<String, ?>>> sendToKafka(Page<Outbox> entryList) {
        ArrayList<CompletableFuture<SendResult<String, ?>>> futureList = new ArrayList<>();
        for (var entry : entryList) {
            futureList.add(sendToKafka(entry));
        }
        return futureList;
    }

    private CompletableFuture<SendResult<String,?>> sendToKafka(Outbox entry) {
        MessageType messageType = entry.getMessageType();
        var dispatcherStrategy = dispatcherStrategies.get(messageType);
        if (dispatcherStrategy != null) {
            return dispatcherStrategy.send(entry);
        } else {
            throw new InvalidOutboxDataException("Unrecognized message type " + messageType);
        }
    }

}
