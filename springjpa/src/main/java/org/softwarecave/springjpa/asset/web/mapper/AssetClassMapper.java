package org.softwarecave.springjpa.asset.web.mapper;

import org.mapstruct.Mapper;
import org.softwarecave.springjpa.asset.model.AssetClass;
import org.softwarecave.springjpa.openapi.model.CreateAssetClassRequest;

@Mapper
public interface AssetClassMapper {

    org.softwarecave.springjpa.openapi.model.AssetClass toApiModel(AssetClass assetClass);
    AssetClass toModel(org.softwarecave.springjpa.openapi.model.AssetClass assetClass);
    AssetClass toModel(CreateAssetClassRequest assetClass);
}
