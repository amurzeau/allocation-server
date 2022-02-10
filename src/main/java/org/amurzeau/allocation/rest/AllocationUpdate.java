package org.amurzeau.allocation.rest;

import java.math.BigDecimal;


import io.quarkus.hibernate.reactive.panache.PanacheEntity;


public class AllocationUpdate extends PanacheEntity {
    public Long projectId;
    
    public String activityTypeId;
    
    public BigDecimal duration;
}
