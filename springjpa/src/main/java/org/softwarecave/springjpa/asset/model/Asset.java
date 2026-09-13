package org.softwarecave.springjpa.asset.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.softwarecave.springjpa.reference.model.AssetReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "asset")
@Getter
@Setter
@NoArgsConstructor
public class Asset {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "asset_class_id")
    @NotNull
    private AssetClass assetClass;

    @OneToMany(mappedBy = "asset")
    private List<AssetReference> references = new ArrayList<>();

    @Version
    @Column(name = "version")
    private Long version;

    public Asset(UUID id, String name, String description, AssetClass assetClass, List<AssetReference> references) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.assetClass = assetClass;
        this.references = references;
    }
}
