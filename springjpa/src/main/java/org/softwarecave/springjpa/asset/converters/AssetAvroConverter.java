package org.softwarecave.springjpa.asset.converters;

import io.micrometer.common.util.StringUtils;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.softwarecave.common.avro.AssetAction;
import org.softwarecave.common.avro.AssetEvent;
import org.softwarecave.springjpa.asset.messaging.consumer.IncomingAssetValidationException;
import org.softwarecave.springjpa.asset.model.Asset;
import org.softwarecave.springjpa.reference.model.AssetReference;
import org.springframework.stereotype.Component;

@Component
public class AssetAvroConverter {
    public AssetEvent toAssetAvro(Asset savedAsset) {
        var references = savedAsset.getReferences().stream()
                .map(r -> org.softwarecave.common.avro.AssetReference.newBuilder()
                        .setId(r.getId())
                        .setName(r.getName())
                        .setValue(r.getValueString())
                        .build())
                .toList();

        return AssetEvent.newBuilder()
                .setAsset(org.softwarecave.common.avro.Asset.newBuilder()
                        .setId(savedAsset.getId())
                        .setName(savedAsset.getName())
                        .setDescription(savedAsset.getDescription())
                        .setReferences(references)
                        .build())
                .setAction(AssetAction.ADD)
                .build();
    }

    public Asset toAsset(org.softwarecave.common.avro.Asset asset) {
        if (asset == null) {
            throw new IncomingAssetValidationException("Incoming asset must not be null");
        }
        if (StringUtils.isBlank(asset.getName())) {
            throw new IncomingAssetValidationException("Name of the incoming asset must not be null");
        }
        if (StringUtils.isBlank(asset.getDescription())) {
            throw new IncomingAssetValidationException("Description of the incoming asset must not be null");
        }

        var result = new Asset(asset.getId(), asset.getName(), asset.getDescription(), null, new ArrayList<>());

        var references = asset.getReferences().stream()
                .map(r -> toAssetReference(r, result))
                .collect(Collectors.toList());
        result.setReferences(references);

        return result;
    }

    private static AssetReference toAssetReference(org.softwarecave.common.avro.AssetReference reference, Asset asset) {
        if (StringUtils.isBlank(reference.getName())) {
            throw new IncomingAssetValidationException("Name of the incoming asset must not be null");
        }
        if (StringUtils.isBlank(reference.getValue())) {
            throw new IncomingAssetValidationException("Value of the incoming asset must not be null");
        }

        return new AssetReference(reference.getId(), reference.getName(), reference.getValue(), asset);
    }
}
