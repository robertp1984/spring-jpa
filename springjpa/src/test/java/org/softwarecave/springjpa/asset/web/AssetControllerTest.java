package org.softwarecave.springjpa.asset.web;

import org.junit.jupiter.api.Test;
import org.softwarecave.springjpa.asset.model.Asset;
import org.softwarecave.springjpa.asset.model.AssetClass;
import org.softwarecave.springjpa.asset.service.AssetService;
import org.softwarecave.springjpa.asset.web.mapper.AssetClassMapperImpl;
import org.softwarecave.springjpa.asset.web.mapper.AssetMapperImpl;
import org.softwarecave.springjpa.asset.web.mapper.AssetPageMapperImpl;
import org.softwarecave.springjpa.common.UUIDGenerator;
import org.softwarecave.springjpa.security.Role;
import org.softwarecave.springjpa.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssetController.class)
@Import({AssetMapperImpl.class, AssetClassMapperImpl.class, AssetPageMapperImpl.class})
class AssetControllerTest {

    private static final String EXTERNAL = "ext";
    public static final UUID UUID_1 = UUIDGenerator.get();
    public static final UUID UUID_2 = UUIDGenerator.get();
    public static final UUID UUID_3 = UUIDGenerator.get();

    @MockitoBean
    private AssetService assetService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetAssetsByFilter_name() throws Exception {
        // given
        var assetClasses = createAssetClasses();
        var assets = createAssets(assetClasses);

        when(assetService.findFiltered(anyString(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(assets.getFirst())));

        // when
        mockMvc.perform(get("/api/v1/assets")
                        .with(AuthUtils.jwtAuth(Role.ASSET_READ.title()))
                        .queryParam("name", "Google"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(UUID_1.toString()))
                .andExpect(jsonPath("$.content[0].name").value("Google"))
                .andExpect(jsonPath("$.content[0].description").value("Google shares"));

        verify(assetService).findFiltered(anyString(), isNull(), any(Pageable.class));
    }

    @Test
    void testGetAssetsByFilter_assetClassName() throws Exception {
        // given
        var assetClasses = createAssetClasses();
        var assets = createAssets(assetClasses);
        when(assetService.findFiltered(isNull(), anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(assets.getLast())));

        // when
        mockMvc.perform(get("/api/v1/assets")
                        .with(AuthUtils.jwtAuth(Role.ASSET_READ.title()))
                        .queryParam("assetClassName", "Microsoft"))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.content[0].id").value(UUID_2.toString()))
                .andExpect(jsonPath("$.content[0].name").value("Microsoft"))
                .andExpect(jsonPath("$.content[0].description").value("Microsoft bonds"))
                .andExpect(jsonPath("$.content[0].assetClass.name").value(EXTERNAL));

        verify(assetService).findFiltered(isNull(), anyString(), any(Pageable.class));
    }

    private static List<Asset> createAssets(List<AssetClass> assetClasses) {
        return List.of(new Asset(UUID_1, "Google", "Google shares", assetClasses.getFirst(), List.of()),
                new Asset(UUID_2, "Microsoft", "Microsoft bonds", assetClasses.getFirst(), List.of()));
    }

    private static List<AssetClass> createAssetClasses() {
        return List.of(new AssetClass(UUID_3, EXTERNAL, "desc"));
    }
}
