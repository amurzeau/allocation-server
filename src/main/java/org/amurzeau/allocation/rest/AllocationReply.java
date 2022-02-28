package org.amurzeau.allocation.rest;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.experimental.FieldNameConstants;

@FieldNameConstants
@Entity
@RegisterForReflection
public class AllocationReply extends PanacheEntity {
    @ManyToOne
    public ProjectReply project;

    @ManyToOne
    public ActivityType activityType;

    public BigDecimal duration;
}
