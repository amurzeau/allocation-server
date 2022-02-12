package org.amurzeau.allocation.rest;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import lombok.experimental.FieldNameConstants;

@FieldNameConstants
@Entity
public class AllocationReply extends PanacheEntity {
    @ManyToOne
    public ProjectReply project;

    @ManyToOne
    public ActivityType activityType;

    public BigDecimal duration;
}
