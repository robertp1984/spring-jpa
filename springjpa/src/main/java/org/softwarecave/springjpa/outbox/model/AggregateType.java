package org.softwarecave.springjpa.outbox.model;

import lombok.Getter;
import org.apache.avro.specific.SpecificRecord;
import org.softwarecave.common.avro.AssetEvent;

@Getter
public enum AggregateType {
    ASSET_EVENT(AssetEvent.class);

    private final Class<? extends SpecificRecord> avroClass;

    AggregateType(Class<? extends SpecificRecord> avroClass) {
        this.avroClass = avroClass;
    }

}
