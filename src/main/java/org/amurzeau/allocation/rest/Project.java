package org.amurzeau.allocation.rest;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import lombok.experimental.FieldNameConstants;


@FieldNameConstants
@Entity
public class Project extends PanacheEntity {
    public String name;
    
    public String board;
    
    public String component;
    
    public String arch;

    @ManyToOne
    public ApplicationType type;

    @ManyToMany
    @JoinTable(name = "Project_Eotp_eotpOpen")
    public List<Eotp> eotpOpen;

    @ManyToMany
    @JoinTable(name = "Project_Eotp_eotpClosed")
    public List<Eotp> eotpClosed;
}
