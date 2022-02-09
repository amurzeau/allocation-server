package org.amurzeau.allocation;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.amurzeau.allocation.rest.Project;

import io.smallrye.mutiny.Uni;

@Path("/project")
public class ProjectRessource {
    @GET
    public Uni<List<Project>> getAll() {
        return Project.findAll().list();
    }
    
    @GET
    @Path("{id}")
    public Uni<Project> getById(Long id) {
        return Project.findById(id);
    }
}