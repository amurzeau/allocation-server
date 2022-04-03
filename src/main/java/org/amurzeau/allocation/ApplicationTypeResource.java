package org.amurzeau.allocation;

import javax.ws.rs.Path;

import org.amurzeau.allocation.rest.ApplicationType;

@Path("/application-types")
public class ApplicationTypeResource extends NamedItemRessource<ApplicationType> {
    public ApplicationTypeResource() {
        super(ApplicationType.class);
    }
}
