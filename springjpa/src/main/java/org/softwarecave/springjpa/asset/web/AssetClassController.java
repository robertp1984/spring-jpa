package org.softwarecave.springjpa.asset.web;

import org.mapstruct.factory.Mappers;
import org.softwarecave.springjpa.asset.service.AssetClassService;
import org.softwarecave.springjpa.asset.web.mapper.AssetClassMapper;
import org.softwarecave.springjpa.openapi.api.AssetClassesApi;
import org.softwarecave.springjpa.openapi.model.CreateAssetClassRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1")
public class AssetClassController implements AssetClassesApi {

    private final AssetClassService assetClassService;
    private final AssetClassMapper mapper;

    public AssetClassController(AssetClassService assetClassService) {
        this.assetClassService = assetClassService;
        this.mapper = Mappers.getMapper(AssetClassMapper.class);
    }

    @Override
    public ResponseEntity<Void> createAssetClass(CreateAssetClassRequest assetClassApi) {
        var assetClass = mapper.toModel(assetClassApi);

        var savedAssetClass = assetClassService.add(assetClass);

        var uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}")
                .buildAndExpand(savedAssetClass.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }
}

