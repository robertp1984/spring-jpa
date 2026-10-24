package org.softwarecave.springjpa.asset.service;

import java.util.Optional;
import java.util.UUID;
import org.softwarecave.springjpa.asset.model.Asset;
import org.softwarecave.springjpa.asset.model.AssetShortRef;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AssetRepository extends JpaRepository<Asset, UUID>, JpaSpecificationExecutor<Asset> {

    @EntityGraph(attributePaths = "references", type = EntityGraph.EntityGraphType.LOAD)
    Page<Asset> findByNameLikeOrDescriptionLike(String nameLikeExpr, String descriptionLikeExpr, Pageable pageable);

    @EntityGraph(attributePaths = "references", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Asset> findByName(String name);

    @EntityGraph(attributePaths = "references", type = EntityGraph.EntityGraphType.LOAD)
    Page<Asset> findByAssetClassName(String assetClassName, Pageable pageable);

    Page<AssetShortRef> findShortRefByAssetClassName(String assetClassName, Pageable pageable);

    @EntityGraph(attributePaths = "references", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Asset> findWithReferencesById(UUID id);
}
