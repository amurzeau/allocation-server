package org.amurzeau.allocation.rest;

import java.sql.Date;

import javax.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;

@Entity
@RegisterForReflection
public class SchemaVersion extends PanacheEntity {
    public long version;

    public String description;

    public String script;

    public Date installed_on;

    public boolean success;
}
