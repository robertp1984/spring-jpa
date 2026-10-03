package org.softwarecave.springjpa.outbox.service.dispatch;

import lombok.extern.slf4j.Slf4j;
import org.softwarecave.springjpa.outbox.model.Outbox;
import org.softwarecave.springjpa.outbox.model.Status;
import org.softwarecave.springjpa.outbox.service.InvalidOutboxDataException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;


@Service
@Slf4j
public class OutboxDispatcherBackoff {

    private final long backoffInitialMillis;
    private final long backoffMaxMillis;
    private final double backoffMultiplier;
    private final int maxAttempts;

    public OutboxDispatcherBackoff(@Value("${app.outbox.sender.backoff-initial}") long backoffInitialMillis,
                                   @Value("${app.outbox.sender.backoff-max}") long backoffMaxMillis,
                                   @Value("${app.outbox.sender.backoff-multiplier}") double backoffMultiplier,
                                   @Value("${app.outbox.sender.max-attempts}") int maxAttempts) {
        if (backoffInitialMillis <= 0 || backoffMaxMillis < backoffInitialMillis || backoffMultiplier < 1.0 || maxAttempts < 1) {
            throw new IllegalArgumentException("Invalid outbox backoff configuration");
        }
        this.backoffInitialMillis = backoffInitialMillis;
        this.backoffMaxMillis = backoffMaxMillis;
        this.backoffMultiplier = backoffMultiplier;
        this.maxAttempts = maxAttempts;
    }

    public void onFailure(Outbox outbox, Throwable cause) {
        int newAttemptCount = outbox.getAttemptCount() + 1;
        outbox.setAttemptCount(newAttemptCount);

        if (!isRetryable(cause)) {
            log.error("Non-retryable failure of outbox id={}. Set the status to FAILED", outbox.getId());
            outbox.setStatus(Status.FAILED);
        } else if (newAttemptCount >= maxAttempts) {
            log.error("Outbox id={} reached the maximum number of attempts {}. Set the status to FAILED",
                    outbox.getId(), maxAttempts);
            outbox.setStatus(Status.FAILED);
        } else {
            long nextAttemptDelayMs = computeDelayMillis(newAttemptCount);
            log.info("Attempt {} of outbox id={} failed. Next attempt in {} ms",
                    newAttemptCount, outbox.getId(), nextAttemptDelayMs);
            outbox.setNextAttemptAt(Instant.now().plusMillis(nextAttemptDelayMs));
        }
    }

    long computeDelayMillis(int attemptCount) {
        double exponentialDelay = backoffInitialMillis * Math.pow(backoffMultiplier, attemptCount - 1);
        long cappedDelay = (long) Math.min(backoffMaxMillis, exponentialDelay);
        // "Equal jitter": keep at least half of the delay and randomize the rest to spread out retries
        long half = cappedDelay / 2;
        return half + ThreadLocalRandom.current().nextLong(cappedDelay - half + 1);
    }

    private static boolean isRetryable(Throwable cause) {
        for (Throwable t = cause; t != null; t = t.getCause()) {
            if (t instanceof InvalidOutboxDataException) {
                return false;
            }
        }
        return true;
    }
}
