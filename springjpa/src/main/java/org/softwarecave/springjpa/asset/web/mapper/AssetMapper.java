package org.softwarecave.springjpa.asset.web.mapper;

import org.mapstruct.Mapper;
import org.softwarecave.springjpa.asset.model.Asset;
import org.softwarecave.springjpa.openapi.model.CreateAssetRequest;

@Mapper(componentModel = "spring", uses = AssetClassMapper.class)
public interface AssetMapper {

    org.softwarecave.springjpa.openapi.model.Asset toApiModel(Asset asset);
    Asset toModel(org.softwarecave.springjpa.openapi.model.Asset asset);
    Asset toModel(CreateAssetRequest asset);
}
