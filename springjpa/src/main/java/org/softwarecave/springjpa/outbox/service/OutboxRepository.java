package org.softwarecave.springjpa.outbox.service;

import jakarta.persistence.LockModeType;
import org.softwarecave.springjpa.outbox.model.Outbox;
import org.softwarecave.springjpa.outbox.model.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.UUID;

public interface OutboxRepository extends JpaRepository<Outbox, UUID> {
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    Page<Outbox> findByStatus(Status status, Pageable pageable);
}
