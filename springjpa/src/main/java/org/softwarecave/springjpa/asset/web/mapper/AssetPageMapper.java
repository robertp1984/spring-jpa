package org.softwarecave.springjpa.asset.web.mapper;

import org.mapstruct.Mapper;
import org.softwarecave.springjpa.asset.model.Asset;
import org.softwarecave.springjpa.openapi.model.AssetPage;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", uses = AssetMapper.class)
public interface AssetPageMapper {
    AssetPage toAssetPageApi(Page<Asset> assetPage);
}
