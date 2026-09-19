package org.softwarecave.springjpa.asset.messaging.consumer;

import org.softwarecave.springjpa.service.DataValidationException;

public class IncomingAssetValidationException extends DataValidationException {
    public IncomingAssetValidationException(String message) {
        super(message);
    }

    public IncomingAssetValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
