package org.amurzeau.allocation.rest;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;

@MappedSuperclass
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
public class NamedItem extends PanacheEntityBase {
    @Id
    public String id;

    public String name;

    public Boolean isDisabled;
}
