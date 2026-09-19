package org.softwarecave.springjpa.asset.messaging.consumer;

import org.softwarecave.springjpa.service.ProcessingException;

public class IncomingAssetProcessingException extends ProcessingException {
    public IncomingAssetProcessingException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
