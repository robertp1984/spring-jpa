package org.softwarecave.springjpa.asset.service;

import org.softwarecave.springjpa.service.NoDataException;

public class NoSuchAssetException extends NoDataException {
    public NoSuchAssetException(String message) {
        super(message);
    }
}
