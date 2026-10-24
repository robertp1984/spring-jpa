package org.softwarecave.springjpa.messaging.exactlyoncedelivery.service;

import java.util.Optional;
import java.util.UUID;
import org.softwarecave.springjpa.messaging.exactlyoncedelivery.model.ExactlyOnceDeliveryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExactlyOnceDeliveryEntryRepository extends JpaRepository<ExactlyOnceDeliveryEntry, UUID> {
    Optional<ExactlyOnceDeliveryEntry> findByMessageIdAndType(UUID messageId, String type);
}
