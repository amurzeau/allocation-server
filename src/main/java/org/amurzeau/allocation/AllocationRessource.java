package org.amurzeau.allocation;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.amurzeau.allocation.rest.Allocation;

import io.smallrye.mutiny.Uni;

@Path("/allocation")
public class AllocationRessource {
    @GET
    public Uni<List<Allocation>> getAll() {
        return Allocation.findAll().list();
    }
    
    @GET
    @Path("{id}")
    public Uni<Allocation> getById(Long id) {
        return Allocation.findById(id);
    }
}