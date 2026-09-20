package org.softwarecave.springjpa.asset.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.softwarecave.springjpa.asset.model.AssetClass;
import org.softwarecave.springjpa.openapi.model.CreateAssetClassRequest;

@Mapper(componentModel = "spring")
public interface AssetClassMapper {

    org.softwarecave.springjpa.openapi.model.AssetClass toApiModel(AssetClass assetClass);

    @Mapping(target = "version", ignore = true)
    AssetClass toModel(org.softwarecave.springjpa.openapi.model.AssetClass assetClass);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    AssetClass toModel(CreateAssetClassRequest assetClass);
}
