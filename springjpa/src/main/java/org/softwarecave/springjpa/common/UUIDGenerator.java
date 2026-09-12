package org.softwarecave.springjpa.common;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.UUID;

public class UUIDGenerator {
    public static UUID get() {
        return UuidCreator.getTimeOrderedEpoch();
    }
}
