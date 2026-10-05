package org.softwarecave.springjpa.outbox.service;

public class InvalidOutboxDataException extends RuntimeException {
    public InvalidOutboxDataException(String message) {
        super(message);
    }

    public InvalidOutboxDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
