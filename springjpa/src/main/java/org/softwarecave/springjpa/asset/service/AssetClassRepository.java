package org.softwarecave.springjpa.asset.service;

import java.util.Optional;
import java.util.UUID;
import org.softwarecave.springjpa.asset.model.AssetClass;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetClassRepository extends JpaRepository<AssetClass, UUID> {
    Optional<AssetClass> findByName(String name);
}
