package org.softwarecave.springjpa.asset.service;

import org.softwarecave.springjpa.asset.model.AssetClass;
import org.softwarecave.springjpa.common.UUIDGenerator;
import org.softwarecave.springjpa.service.DataValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AssetClassService {

    private final AssetClassRepository assetClassRepository;

    public AssetClassService(AssetClassRepository assetClassRepository) {
        this.assetClassRepository = assetClassRepository;
    }

    @Transactional(value = "transactionManager")
    public AssetClass add(AssetClass assetClass) {
        if (assetClass.getId() == null) {
            assetClass.setId(UUIDGenerator.get());
            return assetClassRepository.save(assetClass);
        } else {
            throw new DataValidationException("New asset class id must be null");
        }
    }

    public Optional<AssetClass> findByNameOptional(String name) {
        if (name != null) {
            return assetClassRepository.findByName(name);
        } else {
            throw new AssetValidationException("The name of the asset must not be null");
        }
    }

    public AssetClass findByName(String name) {
        return findByNameOptional(name)
                .orElseThrow(() -> new NoSuchAssetClassException("Asset class %s not found".formatted(name)));
    }
}
