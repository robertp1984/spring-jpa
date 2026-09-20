package org.softwarecave.springjpa.outbox.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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
    private UUID id;

    @Column(name = "topic")
    private String topic;

    @Column(name = "message_type")
    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @Column(name = "aggregate_type")
    @Enumerated(EnumType.STRING)
    private AggregateType aggregateType;

    @Column(name = "aggregate_id")
    private String aggregateId;

    @Column(name = "payload_bytes")
    private byte[] payloadBytes;

    @Column(name = "payload_string")
    private String payloadString;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "version")
    @Version
    private Long version;

    public Outbox(UUID id, String topic, MessageType messageType,
                  AggregateType aggregateType, String aggregateId,
                  byte[] payloadBytes, String payloadString,
                  Instant createdDate, Status status) {
        this.id = id;
        this.topic = topic;
        this.messageType = messageType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.payloadBytes = payloadBytes;
        this.payloadString = payloadString;
        this.createdDate = createdDate;
        this.status = status;
    }
}
