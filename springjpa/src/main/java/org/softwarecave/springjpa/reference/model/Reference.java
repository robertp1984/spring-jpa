package org.softwarecave.springjpa.reference.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "reference")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("ref")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class Reference {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    @NotBlank
    private String name;

    @Column(name = "valueString")
    @NotBlank
    private String valueString;
}
