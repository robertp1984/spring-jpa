package org.softwarecave.springjpa.outbox.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox")
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class Outbox {

    @Id
    @Column(name = "id")
    @NotNull
    private UUID id;

    @Column(name = "topic")
    @NotBlank
    private String topic;

    @Column(name = "message_type")
    @Enumerated(EnumType.STRING)
    @NotNull
    private MessageType messageType;

    @Column(name = "aggregate_type")
    @Enumerated(EnumType.STRING)
    @NotNull
    private AggregateType aggregateType;

    @Column(name = "aggregate_id")
    @NotBlank
    private String aggregateId;

    @Column(name = "payload_bytes")
    private byte[] payloadBytes;

    @Column(name = "payload_string")
    private String payloadString;

    @Column(name = "created_date")
    @NotNull
    private Instant createdDate;

    @Column(name = "status")
    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "version")
    @Version
    private Long version;

    public Outbox(UUID id, String topic, MessageType messageType,
                  AggregateType aggregateType, String aggregateId,
                  byte[] payloadBytes, String payloadString,
                  Instant createdDate, Status status) {
        this(id, topic, messageType, aggregateType, aggregateId, payloadBytes, payloadString, createdDate, status, null);
    }
}
