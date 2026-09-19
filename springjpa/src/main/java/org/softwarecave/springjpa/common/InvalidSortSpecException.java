package org.softwarecave.springjpa.common;

import org.softwarecave.springjpa.service.RequestValidationException;

public class InvalidSortSpecException extends RequestValidationException {
    public InvalidSortSpecException(String message) {
        super(message);
    }
}
