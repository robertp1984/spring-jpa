package org.softwarecave.springjpa.reference.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.softwarecave.springjpa.asset.model.Asset;

import java.util.UUID;

@Entity
@DiscriminatorValue("assetref")
@Getter
@Setter
@NoArgsConstructor
public class AssetReference extends Reference {

    @ManyToOne
    @JoinColumn(name = "asset_id")
    @NotNull
    private Asset asset;

    public AssetReference(UUID id, String name, String valueString, Asset asset) {
        super(id, name, valueString);
        this.asset = asset;
    }

}
