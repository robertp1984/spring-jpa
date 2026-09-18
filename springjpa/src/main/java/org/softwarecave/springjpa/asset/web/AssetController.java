package org.softwarecave.springjpa.asset.web;

import org.mapstruct.factory.Mappers;
import org.softwarecave.springjpa.asset.service.AssetService;
import org.softwarecave.springjpa.asset.web.mapper.AssetMapper;
import org.softwarecave.springjpa.asset.web.mapper.AssetPageMapper;
import org.softwarecave.springjpa.openapi.api.AssetsApi;
import org.softwarecave.springjpa.openapi.model.AssetPage;
import org.softwarecave.springjpa.openapi.model.CreateAssetRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AssetController implements AssetsApi {

    private final AssetService assetService;
    private final AssetMapper mapper;
    private final AssetPageMapper assetPageMapper;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
        this.mapper = Mappers.getMapper(AssetMapper.class);
        this.assetPageMapper = Mappers.getMapper(AssetPageMapper.class);
    }

    @Override
    public ResponseEntity<Void> createAsset(CreateAssetRequest assetApi) {
        var asset = mapper.toModel(assetApi);

        var savedAsset = assetService.addAsset(asset);

        var uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}")
                .buildAndExpand(savedAsset.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @Override
    public ResponseEntity<AssetPage> getAssets(String name, String assetClassName, Integer page, Integer size, List<String> sort) {
        var pageable = PageRequest.of(page, size);//TODO: add sort

        var assetPage = assetService.findFiltered(name, assetClassName, pageable);

        var assetPageApi = assetPageMapper.toAssetPageApi(assetPage);
        return ResponseEntity.ok(assetPageApi);
    }
}
