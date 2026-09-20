package org.softwarecave.springjpa.outbox.service.queue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.softwarecave.springjpa.common.UUIDGenerator;
import org.softwarecave.springjpa.outbox.model.AggregateType;
import org.softwarecave.springjpa.outbox.model.MessageType;
import org.softwarecave.springjpa.outbox.model.Outbox;
import org.softwarecave.springjpa.outbox.model.Status;
import org.softwarecave.springjpa.outbox.service.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class JsonOutboxService {

    private final OutboxRepository outboxRepository;
    private final JsonMapper jsonMapper;

    @Transactional(value = "transactionManager")
    public void send(String topic, String key, Object value, AggregateType aggregateType) {
        String payloadString = jsonMapper.writeValueAsString(value);

        save(topic, key, payloadString, aggregateType);
    }

    private void save(String topic, String id, String payloadString, AggregateType aggregateType) {
        Outbox outbox = new Outbox(UUIDGenerator.get(), topic, MessageType.JSON, aggregateType, id,
                null, payloadString, Instant.now(), Status.NEW);

        log.info("Saving to outbox: {}", outbox);
        outboxRepository.save(outbox);
    }

}
