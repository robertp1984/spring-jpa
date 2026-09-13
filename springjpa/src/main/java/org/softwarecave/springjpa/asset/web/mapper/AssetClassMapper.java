package org.softwarecave.springjpa.asset.web.mapper;

import org.mapstruct.Mapper;
import org.softwarecave.springjpa.asset.model.AssetClass;

@Mapper
public interface AssetClassMapper {

    org.softwarecave.springjpa.openapi.model.AssetClass toApiModel(AssetClass assetClass);
    AssetClass toModel(org.softwarecave.springjpa.openapi.model.AssetClass assetClass);
}
