package org.softwarecave.springjpa.asset.service;

import org.junit.jupiter.api.Test;
import org.softwarecave.springjpa.asset.messaging.AssetKafkaPublisher;
import org.softwarecave.springjpa.asset.model.Asset;
import org.softwarecave.springjpa.asset.model.AssetClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("it")
@Transactional(value = "transactionManager")
@Rollback
class AssetServiceTest {

    private static final String EXTERNAL = "External";
    private static final String INTERNAL = "Internal";

    private static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 100);
    private static final Pageable PAGEABLE_SORT_BY_NAME = PageRequest.of(0, 100, Sort.by("name").ascending());

    @Autowired
    private AssetService assetService;

    @MockitoBean
    private AssetKafkaPublisher assetKafkaPublisher;

    @Autowired
    private AssetClassService assetClassService;

    @Test
    void testSaveAndCountMatchingName() {
        createSampleAssets();

        Page<Asset> page = assetService.findByNameOrDescriptionLike("H%", DEFAULT_PAGEABLE);
        assertThat(page)
                .extracting(Asset::getName)
                .hasSize(2)
                .containsExactlyInAnyOrder("HBO", "HTC");
    }

    @Test
    void testSaveAndGetByName() {
        createSampleAssets();

        Optional<Asset> foundAsset = assetService.findByName("Disney");
        assertThat(foundAsset.get())
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "Disney");
    }

    @Test
    void testSaveAndGetByAssetClassName_External() {
        createSampleAssets();
        Page<Asset> page = assetService.findByAssetClassName(EXTERNAL, DEFAULT_PAGEABLE);

        assertThat(page)
                .hasSize(3)
                .extracting(Asset::getName)
                .containsExactlyInAnyOrder("Netflix", "HBO", "HTC");
    }

    @Test
    void testSaveAndGetByAssetClassName_Internal() {
        createSampleAssets();
        Page<Asset> page = assetService.findByAssetClassName(INTERNAL, DEFAULT_PAGEABLE);

        assertThat(page)
                .hasSize(2)
                .extracting(Asset::getName)
                .containsExactlyInAnyOrder("Disney", "CircleK");
    }

    @Test
    void testSaveAndGetAssetShortRefByAssetClassName_existingClassName() {
        // given
        createSampleAssets();

        // when
        var page = assetService.findShortRefByAssetClassName(EXTERNAL, PAGEABLE_SORT_BY_NAME);
        var shortRefByAssetClassName = page.getContent();

        // then
        assertThat(shortRefByAssetClassName).hasSize(3);
        assertThat(shortRefByAssetClassName.get(0))
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("name", "HBO");
        assertThat(shortRefByAssetClassName.get(1))
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("name", "HTC");
        assertThat(shortRefByAssetClassName.get(2))
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("name", "Netflix");
    }

    @Test
    void testSaveAndGetFiltered_existingName() {
        // given
        createSampleAssets();

        // when
        Page<Asset> assets = assetService.findFiltered("HBO", null, DEFAULT_PAGEABLE);

        // then
        assertThat(assets)
                .hasSize(1)
                .anyMatch(a -> "HBO".equals(a.getName()));
    }

    @Test
    void testSaveAndGetFiltered_missingName() {
        // given
        createSampleAssets();

        // when
        Page<Asset> assets = assetService.findFiltered("notexisting", null, DEFAULT_PAGEABLE);

        // then
        assertThat(assets)
                .isEmpty();
    }

    @Test
    void testSaveAndGetFiltered_existingAssetClassName() {
        // given
        createSampleAssets();

        // when
        Page<Asset> assets = assetService.findFiltered(null, INTERNAL, DEFAULT_PAGEABLE);

        // then
        assertThat(assets)
                .hasSize(2)
                .extracting(Asset::getName)
                .containsExactly("Disney", "CircleK");
    }

    private void createSampleAssets() {
        var externalAssetClass = assetClassService.add(new AssetClass(null, EXTERNAL, "External asset"));
        var internalAssetClass = assetClassService.add(new AssetClass(null, INTERNAL, "Internal asset"));

        assetService.addAsset(new Asset(null, "Netflix", "Netflix shares D", externalAssetClass, List.of()));
        assetService.addAsset(new Asset(null, "HBO", "HBO shares C", externalAssetClass, List.of()));
        assetService.addAsset(new Asset(null, "Disney", "Disney shares A", internalAssetClass, List.of()));
        assetService.addAsset(new Asset(null, "CircleK", "CircleK shares B", internalAssetClass, List.of()));
        assetService.addAsset(new Asset(null, "HTC", "HTC shares A", externalAssetClass, List.of()));
    }
}
