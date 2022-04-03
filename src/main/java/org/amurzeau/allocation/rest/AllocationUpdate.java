package org.amurzeau.allocation.rest;

import java.math.BigDecimal;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class AllocationUpdate extends PanacheEntity {
    public Long projectId;

    public String activityTypeId;

    public BigDecimal duration;
}
