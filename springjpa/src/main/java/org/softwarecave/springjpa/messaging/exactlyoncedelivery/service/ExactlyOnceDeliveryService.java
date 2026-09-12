package org.softwarecave.springjpa.messaging.exactlyoncedelivery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.softwarecave.springjpa.common.UUIDGenerator;
import org.softwarecave.springjpa.messaging.consumer.NonRetryableException;
import org.softwarecave.springjpa.messaging.exactlyoncedelivery.model.ExactlyOnceDeliveryEntry;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExactlyOnceDeliveryService {

    private final ExactlyOnceDeliveryEntryRepository exactlyOnceDeliveryEntryRepository;

    public boolean isDuplicate(UUID messageId, String type) {
        var existingEntry = exactlyOnceDeliveryEntryRepository.findByMessageIdAndType(messageId, type);
        return existingEntry.isPresent();
    }

    private void register(UUID messageId, String type) {
        ExactlyOnceDeliveryEntry entity = new ExactlyOnceDeliveryEntry(UUIDGenerator.get(), messageId, type);
        exactlyOnceDeliveryEntryRepository.save(entity);
    }

    @Transactional(value = "transactionManager")
    public void registerWithCheck(UUID messageId, String type) {
        try {
            register(messageId, type);
        } catch (DataIntegrityViolationException e) {
            log.info("Duplicated message found by messageId={}. Skipping the processing.", messageId);
            throw new NonRetryableException("Duplicated message found by messageId=%s. Skipping the processing.".formatted(messageId),
                    e);
        }
    }
}
