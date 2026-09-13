package org.softwarecave.springjpa.asset.web;

import org.mapstruct.factory.Mappers;
import org.softwarecave.springjpa.asset.service.AssetService;
import org.softwarecave.springjpa.asset.web.mapper.AssetMapper;
import org.softwarecave.springjpa.openapi.api.AssetsApi;
import org.softwarecave.springjpa.openapi.model.Asset;
import org.softwarecave.springjpa.openapi.model.AssetListPage;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1")
public class AssetController implements AssetsApi {

    private final AssetService assetService;
    private final AssetMapper mapper;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
        this.mapper = Mappers.getMapper(AssetMapper.class);
    }

    @Override
    public ResponseEntity<Void> createAsset(Asset assetApi) {
        var asset = mapper.toModel(assetApi);

        var savedAsset = assetService.addAsset(asset);

        var uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}")
                .buildAndExpand(savedAsset.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @Override
    public ResponseEntity<AssetListPage> getAssets(String name, String assetClassName, Integer page, Integer size, String sort) {
        var pageable = PageRequest.of(page, size);//TODO: add sort

        var assetPage = assetService.findFiltered(name, assetClassName, pageable);

        var responseContent = assetPage.getContent().stream()
                .map(mapper::toApiModel)
                .toList();
        var response = new AssetListPage()
                .content(responseContent);
        //TODO: add page information in response
        return ResponseEntity.ok(response);
    }
}
