package org.softwarecave.springjpa.asset.service;

import org.softwarecave.springjpa.service.NoDataException;

public class NoSuchAssetClassException extends NoDataException {
    public NoSuchAssetClassException(String message) {
        super(message);
    }
}
