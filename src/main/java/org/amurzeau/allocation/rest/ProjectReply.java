package org.amurzeau.allocation.rest;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.experimental.FieldNameConstants;

@FieldNameConstants
@Entity
@RegisterForReflection
public class ProjectReply extends PanacheEntity {
    public String name;

    public String board;

    public String component;

    public String arch;

    @ManyToOne
    public ApplicationType type;

    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinTable(name = "Project_Eotp_eotpOpen")
    public Set<Eotp> eotpOpen;

    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinTable(name = "Project_Eotp_eotpClosed")
    public Set<Eotp> eotpClosed;
}
