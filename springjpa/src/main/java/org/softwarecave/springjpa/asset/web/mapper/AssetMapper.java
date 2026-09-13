package org.softwarecave.springjpa.asset.web.mapper;

import org.mapstruct.Mapper;
import org.softwarecave.springjpa.asset.model.Asset;

@Mapper
public interface AssetMapper {

    org.softwarecave.springjpa.openapi.model.Asset toApiModel(Asset asset);
    Asset toModel(org.softwarecave.springjpa.openapi.model.Asset asset);
}
