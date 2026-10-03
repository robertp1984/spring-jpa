package org.softwarecave.springjpa.outbox.service;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.softwarecave.springjpa.outbox.model.Outbox;
import org.softwarecave.springjpa.outbox.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<Outbox, UUID> {
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("""
            select o from Outbox o where o.status = :status
            and o.nextAttemptAt <= :thresholdInstant
            ORDER BY o.createdDate asc LIMIT :limit
            """)
    List<Outbox> findByStatusAndNextAttempt(@Param("status") Status status,
                                            @Param("thresholdInstant") Instant thresholdInstant,
                                            @Param("limit") int limit);

}

