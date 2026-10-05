package org.softwarecave.springjpa.outbox.service.dispatch;

import org.junit.jupiter.api.Test;
import org.softwarecave.springjpa.outbox.model.AggregateType;
import org.softwarecave.springjpa.outbox.model.MessageType;
import org.softwarecave.springjpa.outbox.model.Outbox;
import org.softwarecave.springjpa.outbox.model.Status;
import org.softwarecave.springjpa.outbox.service.InvalidOutboxDataException;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutboxDispatcherBackoffTest {

    private final OutboxDispatcherBackoff backoff = new OutboxDispatcherBackoff(1000, 60000, 2.0, 5);

    @Test
    void testDelayGrowsExponentiallyWithJitter() {
        assertThat(backoff.computeDelayMillis(1)).isBetween(500L, 1000L);
        assertThat(backoff.computeDelayMillis(2)).isBetween(1000L, 2000L);
        assertThat(backoff.computeDelayMillis(3)).isBetween(2000L, 4000L);
    }

    @Test
    void testDelayIsCapped() {
        assertThat(backoff.computeDelayMillis(10)).isBetween(30000L, 60000L);
        assertThat(backoff.computeDelayMillis(1000)).isBetween(30000L, 60000L);
    }

    @Test
    void testRetryableFailureReschedules() {
        Outbox outbox = newOutbox(0);
        Instant before = Instant.now();

        backoff.onFailure(outbox, new TimeoutException());

        assertThat(outbox.getStatus()).isEqualTo(Status.NEW);
        assertThat(outbox.getAttemptCount()).isEqualTo(1);
        assertThat(outbox.getNextAttemptAt()).isBetween(before.plusMillis(500), Instant.now().plusMillis(1000));
    }

    @Test
    void testMaxAttemptsMarksAsFailed() {
        Outbox outbox = newOutbox(4);

        backoff.onFailure(outbox, new TimeoutException());

        assertThat(outbox.getStatus()).isEqualTo(Status.FAILED);
        assertThat(outbox.getAttemptCount()).isEqualTo(5);
    }

    @Test
    void testNonRetryableFailureMarksAsFailedImmediately() {
        Outbox outbox = newOutbox(0);

        backoff.onFailure(outbox, new RuntimeException(new InvalidOutboxDataException("bad payload")));

        assertThat(outbox.getStatus()).isEqualTo(Status.FAILED);
        assertThat(outbox.getAttemptCount()).isEqualTo(1);
    }

    @Test
    void testInvalidConfigurationIsRejected() {
        assertThatThrownBy(() -> new OutboxDispatcherBackoff(0, 1000, 2.0, 5))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OutboxDispatcherBackoff(1000, 500, 2.0, 5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Outbox newOutbox(int attemptCount) {
        Instant now = Instant.now().minus(Duration.ofMinutes(1));
        return new Outbox(UUID.randomUUID(), "topic", MessageType.AVRO, AggregateType.ASSET_EVENT, "key",
                new byte[0], null, now, Status.NEW, attemptCount, now);
    }
}
