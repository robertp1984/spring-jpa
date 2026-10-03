package org.softwarecave.springjpa.outbox.service.dispatch;

import lombok.extern.slf4j.Slf4j;
import org.softwarecave.springjpa.outbox.model.Outbox;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class OutboxDispatcherBackoff {

    private final long backoffInitial;

    public OutboxDispatcherBackoff(@Value("${app.outbox.sender.backoff-initial}") long backoffInitialMillis) {
        this.backoffInitial = backoffInitialMillis;
    }

    public void onFailure(Outbox outbox) {
        int newAttemptCount = outbox.getAttemptCount() + 1;
        log.info("Increase the attempt count of outbox id={} to {}", outbox.getId(), newAttemptCount);
        outbox.setAttemptCount(newAttemptCount);

        double multiplier = Math.max(1.0, newAttemptCount);
        long nextAttemptDelayMs = (long) (multiplier * backoffInitial);
        outbox.setNextAttemptAt(Instant.now().plusMillis(nextAttemptDelayMs));
    }
}
