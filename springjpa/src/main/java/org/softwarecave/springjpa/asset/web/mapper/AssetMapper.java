package org.softwarecave.springjpa.asset.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.softwarecave.springjpa.asset.model.Asset;
import org.softwarecave.springjpa.openapi.model.CreateAssetRequest;

@Mapper(componentModel = "spring", uses = AssetClassMapper.class)
public interface AssetMapper {

    org.softwarecave.springjpa.openapi.model.Asset toApiModel(Asset asset);
    Asset toModel(org.softwarecave.springjpa.openapi.model.Asset asset);

    @Mapping(target = "id", ignore=true)
    @Mapping(target = "references", ignore = true)
    @Mapping(target = "assetClass", ignore = true)
    Asset toModel(CreateAssetRequest asset);
}
