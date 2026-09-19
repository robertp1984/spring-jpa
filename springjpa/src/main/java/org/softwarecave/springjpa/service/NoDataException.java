package org.softwarecave.springjpa.service;

public class NoDataException extends RuntimeException {
    public NoDataException(String message) {
        super(message);
    }
}
