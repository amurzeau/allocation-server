package org.amurzeau.allocation.rest;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotBlank;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;

@MappedSuperclass
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
public class NamedItem extends PanacheEntityBase {
    @Id
    public String id;

    @NotBlank
    public String name;

    public Boolean isDisabled;
}
