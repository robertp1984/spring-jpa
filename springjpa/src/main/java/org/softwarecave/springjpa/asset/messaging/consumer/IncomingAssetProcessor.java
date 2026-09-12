package org.softwarecave.springjpa.asset.messaging.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.softwarecave.common.avro.AssetEvent;
import org.softwarecave.springjpa.asset.converters.AssetAvroConverter;
import org.softwarecave.springjpa.asset.model.Asset;
import org.softwarecave.springjpa.asset.model.AssetClass;
import org.softwarecave.springjpa.asset.service.AssetClassService;
import org.softwarecave.springjpa.asset.service.AssetService;
import org.softwarecave.springjpa.asset.service.AssetValidationException;
import org.softwarecave.springjpa.messaging.exactlyoncedelivery.service.ExactlyOnceDeliveryService;
import org.softwarecave.springjpa.reference.model.AssetReference;
import org.softwarecave.springjpa.reference.service.AssetReferenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class IncomingAssetProcessor {
    public static final String INCOMING = "Incoming";
    private final AssetAvroConverter assetAvroConverter;
    private final AssetService assetService;
    private final AssetClassService assetClassService;
    private final AssetReferenceService assetReferenceService;
    private final ExactlyOnceDeliveryService exactlyOnceDeliveryService;

    @Transactional(value = "transactionManager")
    public void handleIncomingAsset(AssetEvent event, UUID messageId) {
        validate(event);

        // preliminary check for duplicates
        if (exactlyOnceDeliveryService.isDuplicate(messageId, "asset")) {
            log.info("Duplicated message found by messageId={}. Skipping the processing.", messageId);
            return;
        }

        Asset asset;
        try {
            asset = saveAsset(event, messageId);
        } catch (Exception e) {
            log.error("Failed to save the asset into database", e);
            throw new IncomingAssetProcessingException("Failed to save the asset into database", e);
        }

        // final check for duplicates with throwing
        exactlyOnceDeliveryService.registerWithCheck(messageId, "asset");

        log.debug("Saved new asset {} into database", asset);
    }

    private Asset saveAsset(AssetEvent event, UUID messageId) {
        Asset asset = assetAvroConverter.toAsset(event.getAsset());
        asset.setId(null);

        AssetClass assetClass = getAssetClass(asset);
        asset.setAssetClass(assetClass);

        asset.getReferences().add(new AssetReference(null, "incomingAssetId", messageId.toString(), asset));

        log.debug("Saving new asset {} into database", asset);
        assetService.addAssetWithReferences(asset);

        return asset;
    }

    private AssetClass getAssetClass(Asset asset) {
        Optional<AssetClass> assetClass = assetClassService.findByNameOptional(INCOMING);
        return assetClass.orElseGet(() -> assetClassService.add(new AssetClass(null, INCOMING, INCOMING)));
    }

    private static void validate(AssetEvent event) {
        if (event == null || event.getAsset() == null) {
            throw new AssetValidationException("Asset is null in incoming asset");
        }

        var asset = event.getAsset();
        if (asset.getName() == null || asset.getDescription() == null) {
            throw new AssetValidationException("Name and description must not be null");
        }
    }
}
