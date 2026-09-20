package org.softwarecave.springjpa.outbox.service;

import org.softwarecave.springjpa.outbox.model.Outbox;
import org.softwarecave.springjpa.outbox.model.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OutboxRepository extends JpaRepository<Outbox, UUID> {
    Page<Outbox> findByStatus(Status status, Pageable pageable);
}
