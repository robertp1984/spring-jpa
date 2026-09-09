package org.softwarecave.springjpa.asset.service;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.softwarecave.springjpa.asset.messaging.AssetKafkaPublisher;
import org.softwarecave.springjpa.asset.model.Asset;
import org.softwarecave.springjpa.asset.model.AssetShortRef;
import org.softwarecave.springjpa.reference.model.AssetReference;
import org.softwarecave.springjpa.reference.service.AssetReferenceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetReferenceService assetReferenceService;

    private final AssetKafkaPublisher assetKafkaPublisher;

    @Transactional(value = "transactionManager")
    public Asset addAsset(Asset asset) {
        validateNewAsset(asset);
        asset.setId(UuidCreator.getTimeOrderedEpoch());
        var savedAsset = assetRepository.save(asset);
        assetKafkaPublisher.sendAdded(savedAsset);
        return savedAsset;
    }

    @Transactional(value = "transactionManager")
    public Asset addAssetWithReferences(Asset asset) {
        validateNewAsset(asset);
        asset.setId(UuidCreator.getTimeOrderedEpoch());
        var savedAsset = assetRepository.save(asset);

        List<AssetReference> savedAssetReferences = assetReferenceService.saveAll(asset.getReferences());
        savedAsset.setReferences(savedAssetReferences);

        assetKafkaPublisher.sendAdded(savedAsset);
        return savedAsset;
    }

    public Asset findById(UUID id) {
        validateAssetId(id);
        return assetRepository.findWithReferencesById(id)
                .orElseThrow(() -> new NoSuchAssetException("Asset with id %s was not found".formatted(id)));
    }

    private void validateAssetId(UUID id) {
        if (id == null) {
            throw new AssetValidationException("Asset id must not be null");
        }
    }

    public Page<Asset> findByNameOrDescriptionLike(String likeExpr, Pageable pageable) {
        return assetRepository.findByNameLikeOrDescriptionLike(likeExpr, likeExpr, pageable);
    }

    public Optional<Asset> findByName(String name) {
        validateAssetName(name);
        return assetRepository.findByName(name);
    }

    public Page<Asset> findByAssetClassName(String assetClassName, Pageable pageable) {
        validateAssetClassName(assetClassName);
        return assetRepository.findByAssetClassName(assetClassName, pageable);
    }

    public Page<AssetShortRef> findShortRefByAssetClassName(String assetClassName, Pageable pageable) {
        validateAssetClassName(assetClassName);
        return assetRepository.findShortRefByAssetClassName(assetClassName, pageable);
    }

    private void validateAssetName(String name) {
        if (name == null) {
            throw new AssetValidationException("Asset name must not be null");
        }
    }

    private static void validateAssetClassName(String assetClassName) {
        if (assetClassName == null) {
            throw new AssetValidationException("Asset class name must not be null");
        }
    }

    private static void validateNewAsset(Asset asset) {
        if (asset.getId() != null) {
            throw new AssetValidationException("Asset being added must have null key");
        }
    }

    public Page<Asset> findFiltered(String name, String assetClassName, Pageable pageable) {
        Specification<Asset> nameSpec = (root, cq, cb) -> {
            if (cq.getResultType() != Long.class && cq.getResultType() != long.class) {
                root.fetch("references", JoinType.LEFT);
            }

            var predicates = new ArrayList<Predicate>();
            if (name != null) {
                predicates.add(cb.equal(root.get("name"), name));
            }
            if (assetClassName != null) {
                predicates.add(cb.equal(root.get("assetClass").get("name"), assetClassName));
            }

            if (!predicates.isEmpty()) {
                return cb.or(predicates);
            } else {
                return cb.conjunction(); // return all
            }
        };
        return assetRepository.findAll(nameSpec, pageable);
    }
}
