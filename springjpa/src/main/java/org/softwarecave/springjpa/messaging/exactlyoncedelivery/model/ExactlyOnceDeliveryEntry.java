package org.softwarecave.springjpa.messaging.exactlyoncedelivery.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "exactly_once_delivery_entry")
@Getter
@Setter
@NoArgsConstructor
public class ExactlyOnceDeliveryEntry {

    @Id
    private UUID id;

    @Column(name = "message_id")
    @NotNull
    private UUID messageId;

    @Column(name = "type")
    @NotBlank
    private String type;

    @Version
    @Column(name = "version")
    private Long version;

    public ExactlyOnceDeliveryEntry(UUID id, UUID messageId, String type) {
        this.id = id;
        this.messageId = messageId;
        this.type = type;
    }
}
