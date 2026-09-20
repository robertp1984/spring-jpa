package org.softwarecave.springjpa.outbox.service.queue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.softwarecave.springjpa.common.UUIDGenerator;
import org.softwarecave.springjpa.outbox.model.AggregateType;
import org.softwarecave.springjpa.outbox.model.MessageType;
import org.softwarecave.springjpa.outbox.model.Outbox;
import org.softwarecave.springjpa.outbox.model.Status;
import org.softwarecave.springjpa.outbox.service.OutboxRepository;
import org.softwarecave.springjpa.outbox.tools.AvroTools;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvroOutboxService {

    private final OutboxRepository outboxRepository;

    @Transactional(value = "transactionManager")
    public void send(String topic, String key, SpecificRecord value, AggregateType aggregateType) {
        byte[] payloadBinary = AvroTools.convertToBytes(value);

        save(topic, key, payloadBinary, aggregateType);
    }

    private void save(String topic, String key, byte[] payloadBinary, AggregateType aggregateType) {
        Outbox outbox = new Outbox(UUIDGenerator.get(), topic, MessageType.AVRO, aggregateType, key,
                payloadBinary, null, Instant.now(), Status.NEW);

        log.info("Saving to outbox: {}", outbox);
        outboxRepository.save(outbox);
    }
}
